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
import org.qifu.fm.dto.command.FmParallelAddSignReassignRequest;
import org.qifu.fm.dto.command.FmTaskAdminReassignRequest;
import org.qifu.fm.dto.command.FmTaskReassignPreviewRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.dto.view.FmTaskReassignPreviewView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
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
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
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
    private final FmParallelAddSignLifecycleService parallelAddSignLifecycleService;
    private final IFmTaskParallelAddSignService parallelBatchService;
    private final IFmTaskParallelAddSignMemberService parallelMemberService;
    private final FmNotificationPublisher notificationPublisher;

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
            IFmAssignmentResolverService assignmentResolverService,
            FmParallelAddSignLifecycleService parallelAddSignLifecycleService,
            IFmTaskParallelAddSignService parallelBatchService,
            IFmTaskParallelAddSignMemberService parallelMemberService,
            FmNotificationPublisher notificationPublisher) {
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
        this.parallelAddSignLifecycleService = parallelAddSignLifecycleService;
        this.parallelBatchService = parallelBatchService;
        this.parallelMemberService = parallelMemberService;
        this.notificationPublisher = notificationPublisher;
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
    public DefaultResult<List<FmOptionView>> reassignOptions(String tenantId)
            throws ServiceException {
        requireOperator();
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("Tenant 不可為空");
        }
        Date now = new Date();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        List<FmOptionView> options = employeeService.selectListByParams(
                parameters, "EMPLOYEE_NO", "ASC").getValue().stream()
                .filter(value -> (value.getEffectiveFrom() == null
                        || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)))
                .filter(value -> activeMembership(tenantId, value.getAccount(), now))
                .map(value -> new FmOptionView(value.getAccount(),
                        value.getEmployeeNo() + " - " + value.getDisplayName()))
                .toList();
        return success(options);
    }

    @Override
    public DefaultResult<List<FmOptionView>> taskReassignOptions(String tenantId)
            throws ServiceException {
        requireReassignOperator();
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("Tenant 不可空白");
        }
        Date now = new Date();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return success(employeeService.selectListByParams(
                parameters, "EMPLOYEE_NO", "ASC").getValue().stream()
                .filter(value -> (value.getEffectiveFrom() == null
                        || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)))
                .filter(value -> activeMembership(tenantId, value.getAccount(), now))
                .map(value -> new FmOptionView(value.getAccount(),
                        value.getEmployeeNo() + " - " + value.getDisplayName()))
                .toList());
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
        parallelAddSignLifecycleService.cancelWaitingForProcess(
                tenantId, process.getProcessInstanceId(), "PROCESS_TERMINATED");
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

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> reassignParallelAddSign(
            String tenantId, FmParallelAddSignReassignRequest request)
            throws ServiceException {
        requireOperator();
        if (request == null || StringUtils.isAnyBlank(
                tenantId, request.taskId(), request.targetAccount(), request.reason())) {
            throw new ServiceException("平行加簽 Task、改派帳號與理由不可為空");
        }
        if (request.reason().trim().length() > 2000) {
            throw new ServiceException("理由不可超過 2000 字");
        }
        FmTaskParallelAddSignMember member = parallelMemberService
                .findPendingByTask(tenantId, request.taskId());
        if (member == null) {
            throw new ServiceException("找不到可改派的平行加簽 Task");
        }
        FmTaskParallelAddSign batch = parallelBatchService
                .selectByPrimaryKey(member.getParallelAddSignOid()).getValueEmptyThrowMessage();
        if (!tenantId.equals(batch.getTenantId()) || !"WAITING".equals(batch.getStatus())) {
            throw new ServiceException("平行加簽批次已結束或 Tenant 不符");
        }
        Task task = taskService.createTaskQuery().taskId(request.taskId()).singleResult();
        if (task == null || !member.getMemberAccount().equals(task.getAssignee())) {
            throw new ServiceException("平行加簽 Task 狀態已改變");
        }
        Date now = new Date();
        FmEmployee target = activeEmployee(tenantId, request.targetAccount().trim(), now);
        if (target.getAccount().equals(member.getMemberAccount())) {
            throw new ServiceException("改派帳號不可與目前加簽人相同");
        }
        if (parallelMemberService.findByBatch(tenantId, batch.getOid()).stream()
                .anyMatch(item -> target.getAccount().equals(item.getMemberAccount()))) {
            throw new ServiceException("改派帳號已在此平行加簽批次中");
        }
        String actor = UserUtils.getCurrentUser().getUserId();
        if (parallelMemberService.reassignPending(
                tenantId, member.getOid(), member.getLockVersion(),
                target.getAccount(), actor) != 1) {
            throw new ServiceException("平行加簽 Task 已被其他操作更新");
        }
        taskService.setAssignee(task.getId(), target.getAccount());
        FmProcessInstance process = requiredProcess(tenantId, batch.getProcessInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        auditLogicService.recordParallelAddSignAction(
                tenantId, process.getProcessInstanceId(), task.getId(),
                batch.getTaskDefinitionKey(), "PARALLEL_ADD_SIGN_REASSIGN",
                "REASSIGNED_TO:" + target.getAccount(), actor,
                formData.getOwnerAccount(), null, request.reason().trim(),
                formData, now);
        notificationPublisher.taskAssigned(
                tenantId, task.getId(), task.getName(),
                List.of(target.getAccount()), actor, now);
        return success(new FmTaskActionResultView(
                task.getId(), "PARALLEL_ADD_SIGN_REASSIGN",
                process.getProcessInstanceId(), "RUNNING"));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> reassignTask(
            String tenantId, FmTaskAdminReassignRequest request)
            throws ServiceException {
        requireReassignOperator();
        if (request == null || StringUtils.isAnyBlank(tenantId, request.taskId(),
                request.targetAccount(), request.reason(), request.requestKey())) {
            throw new ServiceException("Task、改派對象、原因與 request key 不可空白");
        }
        if (request.reason().trim().length() > 2000
                || request.requestKey().trim().length() > 100) {
            throw new ServiceException("改派原因或 request key 超過長度限制");
        }
        Task task = taskService.createTaskQuery().taskId(request.taskId()).singleResult();
        if (task == null || task.getParentTaskId() != null) {
            throw new ServiceException("Task 不存在，或應使用平行加簽專用改派");
        }
        String storedKey = (String) taskService.getVariableLocal(
                task.getId(), "flowMintAdminReassignRequestKey");
        String fingerprint = request.targetAccount().trim() + "\n" + request.reason().trim();
        if (request.requestKey().trim().equals(storedKey)) {
            String storedFingerprint = (String) taskService.getVariableLocal(
                    task.getId(), "flowMintAdminReassignFingerprint");
            if (!fingerprint.equals(storedFingerprint)) {
                throw new ServiceException("相同 request key 不可更換改派對象或原因");
            }
            return success(new FmTaskActionResultView(task.getId(), "ADMIN_REASSIGN",
                    task.getProcessInstanceId(), "RUNNING"));
        }
        if (StringUtils.startsWith(task.getCategory(), "FLOWMINT_INCIDENT:")) {
            throw new ServiceException("Incident Task 請使用 Incident 改派");
        }
        if (DelegationState.PENDING.equals(task.getDelegationState())
                || Boolean.TRUE.equals(taskService.getVariableLocal(
                        task.getId(), "flowMintAddSign"))
                || Boolean.TRUE.equals(taskService.getVariableLocal(
                        task.getId(), FmParallelAddSignStartService.WAITING_VARIABLE))) {
            throw new ServiceException("代理或加簽進行中 Task 不允許一般改派");
        }
        FmProcessInstance process = requiredProcess(
                tenantId, task.getProcessInstanceId());
        FmTaskPolicy policy = taskPolicyService.findByVersion(tenantId,
                process.getProcessDefId(), process.getProcessVersionNo()).stream()
                .filter(value -> task.getTaskDefinitionKey().equals(value.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("Task Policy 不存在"));
        if ("APPLICANT_CORRECTION".equals(policy.getAssignmentMode())) {
            throw new ServiceException("申請人補件 Task 不允許改派");
        }
        Date now = new Date();
        FmEmployee target = activeEmployee(
                tenantId, request.targetAccount().trim(), now);
        if (target.getAccount().equals(task.getAssignee())) {
            throw new ServiceException("新簽核人不可與目前簽核人相同");
        }
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        if (!"ALLOW".equals(policy.getSelfApprovalPolicy())
                && target.getAccount().equals(formData.getOwnerAccount())) {
            throw new ServiceException("節點政策不允許改派給申請人自簽");
        }
        if ("ALL".equals(policy.getAssignmentMode())
                && taskService.createTaskQuery()
                        .processInstanceId(process.getProcessInstanceId())
                        .taskDefinitionKey(task.getTaskDefinitionKey()).list().stream()
                        .filter(value -> !task.getId().equals(value.getId()))
                        .anyMatch(value -> target.getAccount().equals(value.getAssignee()))) {
            throw new ServiceException("改派對象已是同節點的有效會簽人");
        }
        String previous = task.getAssignee();
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
        String actor = UserUtils.getCurrentUser().getUserId();
        String snapshotId = auditLogicService.recordAssignmentSnapshot(
                new FmAssignmentSnapshotCommand(tenantId, formData.getFormDataId(),
                        process.getProcessInstanceId(), task.getId(),
                        task.getTaskDefinitionKey(), "ADMIN_REASSIGN", actor,
                        null, "PROCESS_OPERATIONS;PREVIOUS="
                                + StringUtils.defaultString(previous), "ASSIGNEE",
                        List.of(new FmResolverCandidateView(target.getEmployeeId(),
                                target.getAccount(), target.getDisplayName()))), now);
        auditLogicService.recordTaskAction(tenantId, process.getProcessInstanceId(),
                task.getId(), task.getTaskDefinitionKey(), "ADMIN_REASSIGN",
                "REASSIGNED_FROM:" + StringUtils.defaultString(previous)
                        + ";REASSIGNED_TO:" + target.getAccount(),
                actor, formData.getOwnerAccount(), null, request.reason().trim(),
                formData, assignmentSnapshot(tenantId, snapshotId), now);
        taskService.setVariableLocal(task.getId(),
                "flowMintAdminReassignRequestKey", request.requestKey().trim());
        taskService.setVariableLocal(task.getId(),
                "flowMintAdminReassignFingerprint", fingerprint);
        notificationPublisher.taskReassigned(tenantId, task.getId(), task.getName(),
                request.requestKey().trim(), previous, target.getAccount(), actor, now);
        return success(new FmTaskActionResultView(task.getId(), "ADMIN_REASSIGN",
                process.getProcessInstanceId(), "RUNNING"));
    }

    @Override
    public DefaultResult<FmTaskReassignPreviewView> previewTaskReassign(
            String tenantId, FmTaskReassignPreviewRequest request)
            throws ServiceException {
        requireReassignOperator();
        if (request == null || StringUtils.isAnyBlank(
                tenantId, request.taskId(), request.targetAccount())) {
            throw new ServiceException("Task 與改派對象不可空白");
        }
        Task task = taskService.createTaskQuery().taskId(request.taskId()).singleResult();
        if (task == null || task.getParentTaskId() != null
                || StringUtils.startsWith(task.getCategory(), "FLOWMINT_INCIDENT:")) {
            throw new ServiceException("此 Task 不支援一般管理員改派");
        }
        if (DelegationState.PENDING.equals(task.getDelegationState())
                || Boolean.TRUE.equals(taskService.getVariableLocal(
                        task.getId(), "flowMintAddSign"))
                || Boolean.TRUE.equals(taskService.getVariableLocal(
                        task.getId(), FmParallelAddSignStartService.WAITING_VARIABLE))) {
            throw new ServiceException("代理或加簽進行中 Task 不允許改派");
        }
        FmProcessInstance process = requiredProcess(tenantId, task.getProcessInstanceId());
        FmTaskPolicy policy = taskPolicyService.findByVersion(tenantId,
                process.getProcessDefId(), process.getProcessVersionNo()).stream()
                .filter(value -> task.getTaskDefinitionKey().equals(value.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("Task Policy 不存在"));
        if ("APPLICANT_CORRECTION".equals(policy.getAssignmentMode())) {
            throw new ServiceException("申請人補件 Task 不允許改派");
        }
        FmEmployee target = activeEmployee(
                tenantId, request.targetAccount().trim(), new Date());
        if (target.getAccount().equals(task.getAssignee())) {
            throw new ServiceException("新簽核人不可與目前簽核人相同");
        }
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        if (!"ALLOW".equals(policy.getSelfApprovalPolicy())
                && target.getAccount().equals(formData.getOwnerAccount())) {
            throw new ServiceException("節點政策不允許改派給申請人自簽");
        }
        boolean multi = List.of("ALL", "SEQUENTIAL").contains(policy.getAssignmentMode());
        if ("ALL".equals(policy.getAssignmentMode())
                && taskService.createTaskQuery()
                        .processInstanceId(process.getProcessInstanceId())
                        .taskDefinitionKey(task.getTaskDefinitionKey()).list().stream()
                        .filter(value -> !task.getId().equals(value.getId()))
                        .anyMatch(value -> target.getAccount().equals(value.getAssignee()))) {
            throw new ServiceException("改派對象已是同節點的有效會簽人");
        }
        String warning = multi
                ? "只改派這一張 active Task，不改變其他會簽 Task 或後續順序"
                : "Task ID、流程節點與期限保持不變";
        return success(new FmTaskReassignPreviewView(task.getId(), task.getName(),
                task.getAssignee(), target.getAccount(), target.getDisplayName(),
                policy.getAssignmentMode(), multi, warning));
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

    private void requireReassignOperator() throws ServiceException {
        if (!UserUtils.isAdmin() && !UserUtils.hasRole("FLOWMINT_REASSIGN")
                && !UserUtils.hasRole("FLOWMINT_OPERATIONS")) {
            throw new ServiceException("需要 FLOWMINT_REASSIGN 管理員改派權限");
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
        if (!activeMembership(tenantId, account, now)) {
            throw new ServiceException("改派帳號不屬於目前 Tenant");
        }
        return employee;
    }

    private boolean activeMembership(String tenantId, String account, Date now) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        return tenantAccountService.selectListByParams(parameters).getValue().stream()
                .anyMatch(value -> (value.getEffectiveFrom() == null
                        || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)));
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
