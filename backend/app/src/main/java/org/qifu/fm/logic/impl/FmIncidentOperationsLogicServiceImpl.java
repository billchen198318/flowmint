package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.RuntimeService;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.dto.command.FmIncidentReassignRequest;
import org.qifu.fm.dto.command.FmIncidentRetryRequest;
import org.qifu.fm.dto.command.FmProcessTerminateRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.domain.resolver.IFmAssignmentResolverService;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmIncidentOperationsLogicService;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmIncidentOperationsLogicServiceImpl
        implements IFmIncidentOperationsLogicService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final FmAssignmentIncidentRecorder incidentRecorder;
    private final IFmEmployeeService employeeService;
    private final IFmTenantAccountService tenantAccountService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmFormDataService formDataService;
    private final IFmTaskAssignmentSnapshotService assignmentSnapshotService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final IFmTaskAssignmentRuleService assignmentRuleService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmAssignmentResolverService assignmentResolverService;

    public FmIncidentOperationsLogicServiceImpl(
            TaskService taskService,
            RuntimeService runtimeService,
            FmAssignmentIncidentRecorder incidentRecorder,
            IFmEmployeeService employeeService,
            IFmTenantAccountService tenantAccountService,
            IFmProcessInstanceService processInstanceService,
            IFmFormDataService formDataService,
            IFmTaskAssignmentSnapshotService assignmentSnapshotService,
            IFmRuntimeAuditLogicService auditLogicService,
            IFmTaskAssignmentRuleService assignmentRuleService,
            IFmTaskPolicyService taskPolicyService,
            IFmAssignmentResolverService assignmentResolverService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.incidentRecorder = incidentRecorder;
        this.employeeService = employeeService;
        this.tenantAccountService = tenantAccountService;
        this.processInstanceService = processInstanceService;
        this.formDataService = formDataService;
        this.assignmentSnapshotService = assignmentSnapshotService;
        this.auditLogicService = auditLogicService;
        this.assignmentRuleService = assignmentRuleService;
        this.taskPolicyService = taskPolicyService;
        this.assignmentResolverService = assignmentResolverService;
    }

    @Override
    public DefaultResult<List<FmAssignmentIncidentView>> incidents(
            String tenantId, String status) throws ServiceException {
        requireOperator();
        if (StringUtils.isBlank(tenantId)
                || (StringUtils.isNotBlank(status)
                        && !List.of("OPEN", "RESOLVED", "IGNORED").contains(status))) {
            throw new ServiceException("Incident 查詢條件不正確");
        }
        return success(incidentRecorder.find(tenantId,
                StringUtils.trimToNull(status)));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> reassign(
            String tenantId, FmIncidentReassignRequest request) throws ServiceException {
        requireOperator();
        if (request == null || StringUtils.isAnyBlank(
                tenantId, request.incidentId(), request.targetAccount(), request.reason())) {
            throw new ServiceException("Incident、改派帳號與理由不可為空");
        }
        if (request.reason().trim().length() > 2000) {
            throw new ServiceException("理由不可超過 2000 字");
        }
        FmAssignmentIncidentView incident = incidentRecorder.requiredOpen(
                tenantId, request.incidentId());
        if (incident == null || StringUtils.isBlank(incident.taskId())) {
            throw new ServiceException("找不到可處理的 OPEN Incident");
        }
        Task task = taskService.createTaskQuery().taskId(incident.taskId()).singleResult();
        if (task == null || !tenantId.equals(task.getTenantId())
                || !("FLOWMINT_INCIDENT:" + incident.incidentId()).equals(task.getCategory())) {
            throw new ServiceException("Incident 對應的 Task 不存在或狀態已改變");
        }
        if (DelegationState.PENDING.equals(task.getDelegationState())) {
            throw new ServiceException("代理中的 Task 不可由 Incident 直接改派");
        }
        Date now = new Date();
        FmEmployee target = activeEmployee(tenantId, request.targetAccount().trim(), now);
        String actor = UserUtils.getCurrentUser().getUserId();
        taskService.getIdentityLinksForTask(task.getId()).stream()
                .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()))
                .forEach(link -> {
                    if (StringUtils.isNotBlank(link.getUserId())) {
                        taskService.deleteCandidateUser(task.getId(), link.getUserId());
                    } else if (StringUtils.isNotBlank(link.getGroupId())) {
                        taskService.deleteCandidateGroup(task.getId(), link.getGroupId());
                    }
                });
        taskService.setAssignee(task.getId(), target.getAccount());
        task.setCategory(null);
        taskService.saveTask(task);

        FmProcessInstance process = requiredProcess(tenantId, incident.processInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        String snapshotId = auditLogicService.recordAssignmentSnapshot(
                new FmAssignmentSnapshotCommand(
                        tenantId, formData.getFormDataId(), process.getProcessInstanceId(),
                        task.getId(), task.getTaskDefinitionKey(), "ADMIN_REASSIGN",
                        actor, null, "INCIDENT:" + incident.incidentId(), "ASSIGNEE",
                        List.of(new FmResolverCandidateView(
                                target.getEmployeeId(), target.getAccount(),
                                target.getDisplayName()))), now);
        FmTaskAssignmentSnapshot snapshot = assignmentSnapshot(tenantId, snapshotId);
        auditLogicService.recordTaskAction(
                tenantId, process.getProcessInstanceId(), task.getId(),
                task.getTaskDefinitionKey(), "ADMIN_REASSIGN",
                "REASSIGNED_TO:" + target.getAccount(), actor,
                formData.getOwnerAccount(), null, request.reason().trim(),
                formData, snapshot, now);
        if (!incidentRecorder.resolve(tenantId, incident.incidentId(), actor,
                request.reason().trim(), now)) {
            throw new ServiceException("Incident 已被其他管理員處理");
        }
        return success(new FmTaskActionResultView(
                task.getId(), "ADMIN_REASSIGN",
                process.getProcessInstanceId(), "RUNNING"));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> retry(
            String tenantId, FmIncidentRetryRequest request) throws ServiceException {
        requireOperator();
        if (request == null || StringUtils.isAnyBlank(
                tenantId, request.incidentId(), request.reason())) {
            throw new ServiceException("Incident 與 Retry 理由不可為空");
        }
        if (request.reason().trim().length() > 1000) {
            throw new ServiceException("Retry 理由不可超過 1000 字");
        }
        FmAssignmentIncidentView incident = incidentRecorder.requiredOpen(
                tenantId, request.incidentId());
        if (incident == null || StringUtils.isBlank(incident.taskId())) {
            throw new ServiceException("找不到可 Retry 的 OPEN Incident");
        }
        Task task = incidentTask(tenantId, incident);
        FmProcessInstance process = requiredProcess(tenantId, incident.processInstanceId());
        Map<String, Object> variables = runtimeService.getVariables(
                process.getProcessInstanceId());
        String initiator = String.valueOf(variables.get(
                FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT));
        List<FmTaskAssignmentRule> rules = assignmentRuleService.findByVersion(
                tenantId, process.getProcessDefId(), process.getProcessVersionNo()).stream()
                .filter(value -> incident.taskDefKey().equals(value.getTaskDefKey()))
                .filter(value -> "ACTIVE".equals(value.getStatus())).toList();
        if (rules.isEmpty()) {
            throw new ServiceException("目前流程版本沒有可用的 Assignment Rule");
        }
        LinkedHashMap<String, FmResolverCandidateView> candidates = new LinkedHashMap<>();
        for (FmTaskAssignmentRule rule : rules) {
            FmResolverPreviewView resolved = assignmentResolverService.resolve(
                    rule, initiator, variables);
            if (!"RESOLVED".equals(resolved.resultStatus())) {
                throw new ServiceException(resolved.message());
            }
            resolved.candidates().forEach(value ->
                    candidates.putIfAbsent(value.account(), value));
        }
        if (candidates.isEmpty()) {
            throw new ServiceException("Retry 仍未解析出有效簽核人");
        }
        FmTaskPolicy policy = taskPolicyService.findByVersion(
                tenantId, process.getProcessDefId(), process.getProcessVersionNo()).stream()
                .filter(value -> incident.taskDefKey().equals(value.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("找不到 Task Policy"));
        applyRetryAssignment(task, policy, new LinkedHashSet<>(candidates.keySet()));
        task.setCategory(null);
        taskService.saveTask(task);
        Date now = new Date();
        String actor = UserUtils.getCurrentUser().getUserId();
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        String snapshotId = auditLogicService.recordAssignmentSnapshot(
                new FmAssignmentSnapshotCommand(
                        tenantId, formData.getFormDataId(), process.getProcessInstanceId(),
                        task.getId(), task.getTaskDefinitionKey(), "INCIDENT_RETRY",
                        actor, null, "INCIDENT:" + incident.incidentId(),
                        "CANDIDATE".equals(policy.getAssignmentMode())
                                ? "CANDIDATE" : "ASSIGNEE",
                        List.copyOf(candidates.values())), now);
        auditLogicService.recordTaskAction(
                tenantId, process.getProcessInstanceId(), task.getId(),
                task.getTaskDefinitionKey(), "ADMIN_REASSIGN", "INCIDENT_RETRY_RESOLVED",
                actor, formData.getOwnerAccount(), null, request.reason().trim(),
                formData, assignmentSnapshot(tenantId, snapshotId), now);
        if (!incidentRecorder.resolve(tenantId, incident.incidentId(), actor,
                "RETRY: " + request.reason().trim(), now)) {
            throw new ServiceException("Incident 已被其他管理員處理");
        }
        return success(new FmTaskActionResultView(
                task.getId(), "ADMIN_REASSIGN",
                process.getProcessInstanceId(), "RUNNING"));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> terminate(
            String tenantId, FmProcessTerminateRequest request) throws ServiceException {
        requireOperator();
        if (request == null || StringUtils.isAnyBlank(
                tenantId, request.processInstanceId(), request.reason())) {
            throw new ServiceException("流程與終止理由不可為空");
        }
        if (request.reason().trim().length() > 1000) {
            throw new ServiceException("終止理由不可超過 1000 字");
        }
        String actor = UserUtils.getCurrentUser().getUserId();
        FmProcessInstance process = requiredProcess(tenantId, request.processInstanceId());
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(process.getProcessInstanceId()).singleResult() == null) {
            throw new ServiceException("Flowable 流程不存在或已結束");
        }
        Date now = new Date();
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        auditLogicService.recordTaskAction(
                tenantId, process.getProcessInstanceId(), null, null,
                "TERMINATE", "TERMINATED", actor, formData.getOwnerAccount(),
                null, request.reason().trim(), formData, null, now);
        runtimeService.deleteProcessInstance(
                process.getProcessInstanceId(), request.reason().trim());
        if (!processInstanceService.updateStatus(
                tenantId, process.getProcessInstanceId(), "RUNNING", "TERMINATED",
                now, actor)) {
            throw new ServiceException("流程已被其他操作處理");
        }
        formData.setDataStatus("CANCELLED");
        formData.setUuserid(actor);
        formData.setUdate(now);
        formDataService.update(formData);
        incidentRecorder.ignoreOpenForProcess(
                tenantId, process.getProcessInstanceId(), actor,
                "PROCESS_TERMINATED: " + request.reason().trim(), now);
        return success(new FmTaskActionResultView(
                null, "TERMINATE", process.getProcessInstanceId(), "TERMINATED"));
    }

    private Task incidentTask(String tenantId, FmAssignmentIncidentView incident)
            throws ServiceException {
        Task task = taskService.createTaskQuery().taskId(incident.taskId()).singleResult();
        if (task == null || !tenantId.equals(task.getTenantId())
                || !("FLOWMINT_INCIDENT:" + incident.incidentId()).equals(task.getCategory())) {
            throw new ServiceException("Incident 對應的 Task 不存在或狀態已改變");
        }
        return task;
    }

    private void applyRetryAssignment(
            Task task, FmTaskPolicy policy, LinkedHashSet<String> accounts)
            throws ServiceException {
        switch (policy.getAssignmentMode()) {
            case "ASSIGNEE" -> taskService.setAssignee(task.getId(), accounts.getFirst());
            case "CANDIDATE" -> accounts.forEach(account ->
                    taskService.addCandidateUser(task.getId(), account));
            case "ALL", "SEQUENTIAL" -> {
                Object selected = taskService.getVariable(task.getId(), "flowmintAssignee");
                if (selected == null || !accounts.contains(selected.toString())) {
                    throw new ServiceException("Multi-instance Retry 簽核人變數不正確");
                }
                taskService.setAssignee(task.getId(), selected.toString());
            }
            default -> throw new ServiceException("此 Task 派送方式不支援 Retry");
        }
    }

    private void requireOperator() throws ServiceException {
        if (!UserUtils.isAdmin() && !UserUtils.hasRole("FLOWMINT_OPERATIONS")) {
            throw new ServiceException("需要流程營運管理權限");
        }
    }

    private FmEmployee activeEmployee(String tenantId, String account, Date now)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        FmEmployee employee = employeeService.selectListByParams(parameters).getValue().stream()
                .filter(value -> (value.getEffectiveFrom() == null
                        || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)))
                .findFirst().orElseThrow(() -> new ServiceException("改派員工不存在或未啟用"));
        boolean activeMembership = tenantAccountService.selectListByParams(parameters)
                .getValue().stream().anyMatch(value ->
                        (value.getEffectiveFrom() == null
                                || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)));
        if (!activeMembership) {
            throw new ServiceException("改派帳號不屬於目前 Tenant");
        }
        return employee;
    }

    private FmProcessInstance requiredProcess(String tenantId, String processInstanceId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processInstanceId", processInstanceId);
        return processInstanceService.selectListByParams(parameters).getValue().stream()
                .filter(value -> "RUNNING".equals(value.getInstanceStatus()))
                .findFirst().orElseThrow(() -> new ServiceException("流程不存在或已結束"));
    }

    private FmFormData requiredFormData(String tenantId, String formDataId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formDataId", formDataId);
        return formDataService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到流程表單資料"));
    }

    private FmTaskAssignmentSnapshot assignmentSnapshot(
            String tenantId, String snapshotId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("assignmentSnapshotId", snapshotId);
        return assignmentSnapshotService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到改派快照"));
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
