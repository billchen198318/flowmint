package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmTaskActionRequest;
import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.dto.command.FmTaskTransferRequest;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmTaskActionView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.dto.view.FmTaskHistoryView;
import org.qifu.fm.dto.view.FmTaskInboxView;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.logic.IFmTaskRuntimeLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskActionService;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskRuntimeLogicServiceImpl implements IFmTaskRuntimeLogicService {

    private static final Set<String> ACTION_TYPES = Set.of(
            "APPROVE", "REJECT", "RETURN", "RESUBMIT");

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IFmTenantAccountService tenantAccountService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmProcessDefService processDefService;
    private final IFmFormDataService formDataService;
    private final IFmEmployeeService employeeService;
    private final IFmFormDefService formDefService;
    private final IFmFormVersionService formVersionService;
    private final IFmTaskFormRuleService taskFormRuleService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmTaskActionService taskActionService;
    private final IFmTaskAssignmentSnapshotService assignmentSnapshotService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final FmFormSubmissionValidator formSubmissionValidator;
    private final ObjectMapper objectMapper;

    public FmTaskRuntimeLogicServiceImpl(
            TaskService taskService,
            RuntimeService runtimeService,
            HistoryService historyService,
            IFmTenantAccountService tenantAccountService,
            IFmProcessInstanceService processInstanceService,
            IFmProcessDefService processDefService,
            IFmFormDataService formDataService,
            IFmEmployeeService employeeService,
            IFmFormDefService formDefService,
            IFmFormVersionService formVersionService,
            IFmTaskFormRuleService taskFormRuleService,
            IFmTaskPolicyService taskPolicyService,
            IFmTaskActionService taskActionService,
            IFmTaskAssignmentSnapshotService assignmentSnapshotService,
            IFmRuntimeAuditLogicService auditLogicService,
            FmFormSubmissionValidator formSubmissionValidator,
            ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.tenantAccountService = tenantAccountService;
        this.processInstanceService = processInstanceService;
        this.processDefService = processDefService;
        this.formDataService = formDataService;
        this.employeeService = employeeService;
        this.formDefService = formDefService;
        this.formVersionService = formVersionService;
        this.taskFormRuleService = taskFormRuleService;
        this.taskPolicyService = taskPolicyService;
        this.taskActionService = taskActionService;
        this.assignmentSnapshotService = assignmentSnapshotService;
        this.auditLogicService = auditLogicService;
        this.formSubmissionValidator = formSubmissionValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public DefaultResult<List<FmTaskInboxView>> inbox(String tenantId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        List<FmTaskInboxView> values = new ArrayList<>();
        for (Task task : taskService.createTaskQuery()
                .taskCandidateOrAssigned(account)
                .orderByTaskCreateTime().desc().list()) {
            FmProcessInstance process = processInstance(tenantId, task.getProcessInstanceId());
            if (process != null && "RUNNING".equals(process.getInstanceStatus())) {
                values.add(inboxView(task, process));
            }
        }
        return success(List.copyOf(values));
    }

    @Override
    public DefaultResult<FmTaskDetailView> load(String tenantId, String taskId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        Task task = authorizedTask(taskId, account);
        FmProcessInstance process = requiredProcess(tenantId, task.getProcessInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        FmTaskFormRule formRule = taskFormRule(process, task.getTaskDefinitionKey());
        FmFormVersion formVersion = formVersion(tenantId, formRule);
        FmFormDef formDef = formDef(tenantId, formRule.getFormId());
        FmTaskPolicy policy = taskPolicy(process, task.getTaskDefinitionKey());
        return success(new FmTaskDetailView(
                inboxView(task, process),
                formRule.getFormId(),
                formRule.getFormVersionNo(),
                formDef.getFormName(),
                formVersion.getSchemaContent(),
                formVersion.getUiSchemaContent(),
                formVersion.getCustomScriptContent(),
                parseData(formData.getDataContent()),
                "APPLICANT_CORRECTION".equals(policy.getAssignmentMode()),
                "Y".equals(policy.getAllowReject()),
                "Y".equals(policy.getAllowReturn()),
                "Y".equals(policy.getAllowTransfer()),
                policy.getCommentRequired(),
                returnTargets(process, task.getTaskDefinitionKey()),
                actionHistory(tenantId, process.getProcessInstanceId())));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> action(
            String tenantId, FmTaskActionRequest request) throws ServiceException {
        validateActionRequest(request);
        String account = currentAccount(tenantId);
        Task task = authorizedTask(request.taskId(), account);
        FmProcessInstance process = requiredProcess(tenantId, task.getProcessInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        FmTaskPolicy policy = taskPolicy(process, task.getTaskDefinitionKey());
        validatePolicy(request, policy, process.getProcessInstanceId(),
                task.getTaskDefinitionKey());
        claimIfRequired(task, account);
        Date now = new Date();
        if ("RESUBMIT".equals(request.actionType())) {
            resubmitForm(request, formData, formVersion(
                    tenantId, taskFormRule(process, task.getTaskDefinitionKey())), account, now);
            runtimeService.setVariable(process.getProcessInstanceId(),
                    org.qifu.fm.flowable.FmTaskAssignmentListener.VARIABLE_FORM_DATA,
                    request.formData());
        }
        FmTaskAssignmentSnapshot assignmentSnapshot = latestAssignmentSnapshot(
                tenantId, task.getId());
        auditLogicService.recordTaskAction(
                tenantId,
                process.getProcessInstanceId(),
                task.getId(),
                task.getTaskDefinitionKey(),
                request.actionType(),
                outcome(request.actionType()),
                account,
                formData.getOwnerAccount(),
                request.comment(),
                request.reason(),
                formData,
                assignmentSnapshot,
                now);
        String status = executeAction(request, task, process, formData, account, now);
        return success(new FmTaskActionResultView(
                task.getId(), request.actionType(),
                process.getProcessInstanceId(), status));
    }

    @Override
    public DefaultResult<List<FmOptionView>> transferOptions(
            String tenantId, String taskId) throws ServiceException {
        String account = currentAccount(tenantId);
        Task task = authorizedTask(taskId, account);
        FmProcessInstance process = requiredProcess(
                tenantId, task.getProcessInstanceId());
        ensureTransferAllowed(taskPolicy(process, task.getTaskDefinitionKey()));
        Date now = new Date();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        List<FmOptionView> values = employeeService.selectListByParams(
                parameters, "EMPLOYEE_NO", "ASC").getValue().stream()
                .filter(employee -> !account.equals(employee.getAccount()))
                .filter(employee -> effective(employee, now))
                .filter(employee -> activeTenantAccount(
                        tenantId, employee.getAccount(), now))
                .map(employee -> new FmOptionView(
                        employee.getAccount(),
                        employee.getEmployeeNo() + " - " + employee.getDisplayName()))
                .toList();
        return success(values);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> transfer(
            String tenantId, FmTaskTransferRequest request) throws ServiceException {
        if (request == null || StringUtils.isAnyBlank(
                request.taskId(), request.targetAccount(), request.reason())) {
            throw new ServiceException("Task、轉派對象與原因不可為空");
        }
        if (request.reason().trim().length() > 1000) {
            throw new ServiceException("轉派原因不可超過 1000 字");
        }
        String account = currentAccount(tenantId);
        String targetAccount = request.targetAccount().trim();
        if (account.equals(targetAccount)) {
            throw new ServiceException("不可將待辦轉派給自己");
        }
        Task task = authorizedTask(request.taskId(), account);
        FmProcessInstance process = requiredProcess(
                tenantId, task.getProcessInstanceId());
        FmTaskPolicy policy = taskPolicy(process, task.getTaskDefinitionKey());
        ensureTransferAllowed(policy);
        Date now = new Date();
        FmEmployee target = activeEmployee(
                tenantId, targetAccount, now);
        claimIfRequired(task, account);
        clearCandidateLinks(task.getId());
        taskService.setAssignee(task.getId(), target.getAccount());
        FmFormData formData = requiredFormData(
                tenantId, process.getFormDataId());
        String snapshotId = auditLogicService.recordAssignmentSnapshot(
                new FmAssignmentSnapshotCommand(
                        tenantId,
                        formData.getFormDataId(),
                        process.getProcessInstanceId(),
                        task.getId(),
                        task.getTaskDefinitionKey(),
                        "TRANSFER",
                        account,
                        null,
                        "TRANSFER_FROM:" + account,
                        "ASSIGNEE",
                        List.of(new FmResolverCandidateView(
                                target.getEmployeeId(), target.getAccount(),
                                target.getDisplayName()))),
                now);
        auditLogicService.recordTaskAction(
                tenantId,
                process.getProcessInstanceId(),
                task.getId(),
                task.getTaskDefinitionKey(),
                "TRANSFER",
                "TRANSFERRED_TO:" + target.getAccount(),
                account,
                formData.getOwnerAccount(),
                request.comment(),
                request.reason().trim(),
                formData,
                assignmentSnapshot(tenantId, snapshotId),
                now);
        return success(new FmTaskActionResultView(
                task.getId(), "TRANSFER",
                process.getProcessInstanceId(), "RUNNING"));
    }

    private String executeAction(
            FmTaskActionRequest request,
            Task task,
            FmProcessInstance process,
            FmFormData formData,
            String account,
            Date now) throws ServiceException {
        if ("APPROVE".equals(request.actionType())) {
            taskService.complete(task.getId());
            boolean running = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(process.getProcessInstanceId()).singleResult() != null;
            if (running) {
                return "RUNNING";
            }
            transitionProcess(process, "COMPLETED", now, account);
            updateFormStatus(formData, "COMPLETED", account, now);
            return "COMPLETED";
        }
        if ("REJECT".equals(request.actionType())) {
            runtimeService.deleteProcessInstance(
                    process.getProcessInstanceId(), request.reason());
            transitionProcess(process, "REJECTED", now, account);
            updateFormStatus(formData, "REJECTED", account, now);
            return "REJECTED";
        }
        if ("RESUBMIT".equals(request.actionType())) {
            taskService.complete(task.getId());
            return "RUNNING";
        }
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(process.getProcessInstanceId())
                .moveActivityIdTo(
                        task.getTaskDefinitionKey(), request.targetTaskDefKey())
                .changeState();
        updateFormStatus(formData, "RETURNED", account, now);
        return "RUNNING";
    }

    private void validateActionRequest(FmTaskActionRequest request)
            throws ServiceException {
        if (request == null || StringUtils.isAnyBlank(
                request.taskId(), request.actionType())
                || !ACTION_TYPES.contains(request.actionType())) {
            throw new ServiceException("簽核動作參數不正確");
        }
    }

    private void validatePolicy(
            FmTaskActionRequest request,
            FmTaskPolicy policy,
            String processInstanceId,
            String currentTaskDefKey) throws ServiceException {
        boolean reject = "REJECT".equals(request.actionType());
        boolean returning = "RETURN".equals(request.actionType());
        boolean resubmit = "RESUBMIT".equals(request.actionType());
        boolean correctionTask = "APPLICANT_CORRECTION".equals(policy.getAssignmentMode());
        if (resubmit != correctionTask) {
            throw new ServiceException("只有申請人補件節點可以重新送出表單");
        }
        if (reject && !"Y".equals(policy.getAllowReject())) {
            throw new ServiceException("此節點不允許駁回");
        }
        if (returning && !"Y".equals(policy.getAllowReturn())) {
            throw new ServiceException("此節點不允許退回");
        }
        boolean commentRequired = "ALWAYS".equals(policy.getCommentRequired())
                || ("ON_REJECT_RETURN".equals(policy.getCommentRequired())
                        && (reject || returning));
        if (commentRequired && StringUtils.isBlank(request.comment())) {
            throw new ServiceException("此簽核動作必須填寫意見");
        }
        if ((reject || returning) && StringUtils.isBlank(request.reason())) {
            throw new ServiceException("駁回或退回必須填寫理由");
        }
        if (returning) {
            boolean validTarget = correctionTargets(policy.getTenantId(),
                    policy.getProcessDefId(), policy.getProcessVersionNo(), currentTaskDefKey)
                    .stream().anyMatch(value -> value.taskDefKey()
                            .equals(request.targetTaskDefKey()));
            if (!validTarget) {
                throw new ServiceException("退回目標不是已完成的前置 User Task");
            }
        }
    }

    private void resubmitForm(FmTaskActionRequest request, FmFormData formData,
            FmFormVersion formVersion, String account, Date now) throws ServiceException {
        formSubmissionValidator.validate(formVersion.getSchemaContent(), request.formData());
        formData.setDataContent(objectMapper.writeValueAsString(request.formData()));
        formData.setRevisionNo(formData.getRevisionNo() + 1);
        formData.setDataStatus("SUBMITTED");
        formData.setUuserid(account);
        formData.setUdate(now);
        formDataService.update(formData);
    }

    private Task authorizedTask(String taskId, String account) throws ServiceException {
        if (StringUtils.isBlank(taskId)) {
            throw new ServiceException("Task ID 不可為空");
        }
        Task task = taskService.createTaskQuery()
                .taskId(taskId).taskCandidateOrAssigned(account).singleResult();
        if (task == null) {
            throw new ServiceException("待辦不存在、已處理或目前帳號無權處理");
        }
        return task;
    }

    private void claimIfRequired(Task task, String account) {
        if (StringUtils.isBlank(task.getAssignee())) {
            taskService.claim(task.getId(), account);
        }
    }

    private void clearCandidateLinks(String taskId) {
        for (IdentityLink link : taskService.getIdentityLinksForTask(taskId)) {
            if (!IdentityLinkType.CANDIDATE.equals(link.getType())) {
                continue;
            }
            if (StringUtils.isNotBlank(link.getUserId())) {
                taskService.deleteCandidateUser(taskId, link.getUserId());
            }
            if (StringUtils.isNotBlank(link.getGroupId())) {
                taskService.deleteCandidateGroup(taskId, link.getGroupId());
            }
        }
    }

    private String currentAccount(String tenantId) throws ServiceException {
        String account = UserUtils.getCurrentUser().getUsername();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        List<FmTenantAccount> values = tenantAccountService
                .selectListByParams(parameters).getValue();
        Date now = new Date();
        boolean active = values.stream().anyMatch(value ->
                (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)));
        if (!active) {
            throw new ServiceException("目前帳號不屬於指定 Tenant");
        }
        return account;
    }

    private void ensureTransferAllowed(FmTaskPolicy policy) throws ServiceException {
        if (!"Y".equals(policy.getAllowTransfer())) {
            throw new ServiceException("此節點不允許轉派");
        }
    }

    private FmEmployee activeEmployee(
            String tenantId, String account, Date now) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        FmEmployee employee = employeeService.selectListByParams(parameters)
                .getValue().stream()
                .filter(value -> effective(value, now))
                .findFirst().orElseThrow(() ->
                        new ServiceException("轉派對象不是有效員工"));
        if (!activeTenantAccount(tenantId, account, now)) {
            throw new ServiceException("轉派對象不具有效 Tenant membership");
        }
        return employee;
    }

    private boolean effective(FmEmployee employee, Date now) {
        return (employee.getEffectiveFrom() == null
                || !employee.getEffectiveFrom().after(now))
                && (employee.getEffectiveTo() == null
                        || employee.getEffectiveTo().after(now));
    }

    private boolean activeTenantAccount(String tenantId, String account, Date now) {
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

    private FmTaskInboxView inboxView(Task task, FmProcessInstance process)
            throws ServiceException {
        FmProcessDef definition = processDef(process.getTenantId(), process.getProcessDefId());
        FmFormData data = requiredFormData(process.getTenantId(), process.getFormDataId());
        return new FmTaskInboxView(
                task.getId(), task.getTaskDefinitionKey(), task.getName(),
                process.getProcessInstanceId(), process.getBusinessKey(),
                definition.getProcessName(), data.getOwnerAccount(),
                task.getCreateTime(), task.getDueDate());
    }

    private FmProcessInstance requiredProcess(String tenantId, String processInstanceId)
            throws ServiceException {
        FmProcessInstance value = processInstance(tenantId, processInstanceId);
        if (value == null || !"RUNNING".equals(value.getInstanceStatus())) {
            throw new ServiceException("流程實例不存在或已結束");
        }
        return value;
    }

    private FmProcessInstance processInstance(String tenantId, String processInstanceId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processInstanceId", processInstanceId);
        return processInstanceService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElse(null);
    }

    private FmFormData requiredFormData(String tenantId, String formDataId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formDataId", formDataId);
        return formDataService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到待辦表單資料"));
    }

    private FmProcessDef processDef(String tenantId, String processDefId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        return processDefService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到流程定義"));
    }

    private FmTaskFormRule taskFormRule(FmProcessInstance process, String taskDefKey)
            throws ServiceException {
        return taskFormRuleService.findByVersion(
                process.getTenantId(), process.getProcessDefId(),
                process.getProcessVersionNo()).stream()
                .filter(value -> taskDefKey.equals(value.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("待辦節點未綁定表單"));
    }

    private FmTaskPolicy taskPolicy(FmProcessInstance process, String taskDefKey)
            throws ServiceException {
        return taskPolicyService.findByVersion(
                process.getTenantId(), process.getProcessDefId(),
                process.getProcessVersionNo()).stream()
                .filter(value -> taskDefKey.equals(value.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("待辦節點缺少政策"));
    }

    private FmFormVersion formVersion(String tenantId, FmTaskFormRule rule)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", rule.getFormId());
        parameters.put("versionNo", rule.getFormVersionNo());
        return formVersionService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到待辦表單版本"));
    }

    private FmFormDef formDef(String tenantId, String formId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        return formDefService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到待辦表單主檔"));
    }

    private List<FmTaskHistoryView> historyTargets(
            String processInstanceId, String currentTaskDefKey) {
        Map<String, FmTaskHistoryView> values = new LinkedHashMap<>();
        for (HistoricTaskInstance task : historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list()) {
            if (!currentTaskDefKey.equals(task.getTaskDefinitionKey())) {
                values.putIfAbsent(task.getTaskDefinitionKey(), new FmTaskHistoryView(
                        task.getTaskDefinitionKey(), task.getName(), task.getAssignee(),
                        task.getStartTime(), task.getEndTime()));
            }
        }
        return List.copyOf(values.values());
    }

    private List<FmTaskHistoryView> returnTargets(
            FmProcessInstance process, String currentTaskDefKey) {
        return correctionTargets(process.getTenantId(), process.getProcessDefId(),
                process.getProcessVersionNo(), currentTaskDefKey);
    }

    private List<FmTaskHistoryView> correctionTargets(String tenantId,
            String processDefId, Integer versionNo, String currentTaskDefKey) {
        return taskPolicyService.findByVersion(tenantId, processDefId, versionNo).stream()
                .filter(value -> !currentTaskDefKey.equals(value.getTaskDefKey()))
                .filter(value -> "APPLICANT_CORRECTION".equals(value.getAssignmentMode()))
                .map(value -> new FmTaskHistoryView(value.getTaskDefKey(),
                        value.getTaskName(), null, null, null))
                .toList();
    }

    private List<FmTaskActionView> actionHistory(
            String tenantId, String processInstanceId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processInstanceId", processInstanceId);
        return taskActionService.selectListByParams(
                parameters, "ACTION_DATE", "ASC").getValue().stream()
                .map(this::actionView).toList();
    }

    private FmTaskActionView actionView(FmTaskAction action) {
        return new FmTaskActionView(
                action.getActionType(), action.getOutcome(),
                action.getActorAccount(), action.getCommentText(),
                action.getReason(), action.getActionDate());
    }

    private FmTaskAssignmentSnapshot latestAssignmentSnapshot(
            String tenantId, String taskId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("taskId", taskId);
        return assignmentSnapshotService.selectListByParams(
                parameters, "RESOLVED_DATE", "DESC").getValue().stream()
                .findFirst().orElse(null);
    }

    private FmTaskAssignmentSnapshot assignmentSnapshot(
            String tenantId, String assignmentSnapshotId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("assignmentSnapshotId", assignmentSnapshotId);
        return assignmentSnapshotService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() ->
                        new ServiceException("轉派指派快照建立失敗"));
    }

    private Map<String, Object> parseData(String content) throws ServiceException {
        try {
            return objectMapper.readValue(
                    content, new TypeReference<Map<String, Object>>() { });
        } catch (RuntimeException exception) {
            throw new ServiceException("表單資料 JSON 格式不正確");
        }
    }

    private void transitionProcess(
            FmProcessInstance process,
            String targetStatus,
            Date endDate,
            String account) throws ServiceException {
        if (!processInstanceService.updateStatus(
                process.getTenantId(), process.getProcessInstanceId(),
                "RUNNING", targetStatus, endDate, account)) {
            throw new ServiceException("流程狀態已被其他操作更新");
        }
    }

    private void updateFormStatus(
            FmFormData formData, String status, String account, Date now) {
        formData.setDataStatus(status);
        formData.setUuserid(account);
        formData.setUdate(now);
        formDataService.update(formData);
    }

    private String outcome(String actionType) {
        return switch (actionType) {
            case "APPROVE" -> "APPROVED";
            case "REJECT" -> "REJECTED";
            case "RETURN" -> "RETURNED";
            case "RESUBMIT" -> "RESUBMITTED";
            default -> actionType;
        };
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
