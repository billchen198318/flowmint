package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.HistoryService;
import org.flowable.identitylink.api.IdentityLinkType;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.view.FmFormSnapshotView;
import org.qifu.fm.dto.view.FmCurrentApprovalPersonView;
import org.qifu.fm.dto.view.FmCurrentApprovalView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackView;
import org.qifu.fm.dto.view.FmTaskActionView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmRequestTrackingLogicService;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskActionService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmRequestTrackingLogicServiceImpl
        implements IFmRequestTrackingLogicService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IFmTenantAccountService tenantAccountService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmProcessDefService processDefService;
    private final IFmProcessVersionService processVersionService;
    private final IFmFormDataService formDataService;
    private final IFmEmployeeService employeeService;
    private final IFmFormDefService formDefService;
    private final IFmFormVersionService formVersionService;
    private final IFmTaskActionService taskActionService;
    private final IFmFormSnapshotService formSnapshotService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final ObjectMapper objectMapper;
    private final FmNotificationPublisher notificationPublisher;

    public FmRequestTrackingLogicServiceImpl(
            TaskService taskService,
            RuntimeService runtimeService,
            HistoryService historyService,
            IFmTenantAccountService tenantAccountService,
            IFmProcessInstanceService processInstanceService,
            IFmProcessDefService processDefService,
            IFmProcessVersionService processVersionService,
            IFmFormDataService formDataService,
            IFmEmployeeService employeeService,
            IFmFormDefService formDefService,
            IFmFormVersionService formVersionService,
            IFmTaskActionService taskActionService,
            IFmFormSnapshotService formSnapshotService,
            IFmRuntimeAuditLogicService auditLogicService,
            FmNotificationPublisher notificationPublisher,
            ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.tenantAccountService = tenantAccountService;
        this.processInstanceService = processInstanceService;
        this.processDefService = processDefService;
        this.processVersionService = processVersionService;
        this.formDataService = formDataService;
        this.employeeService = employeeService;
        this.formDefService = formDefService;
        this.formVersionService = formVersionService;
        this.taskActionService = taskActionService;
        this.formSnapshotService = formSnapshotService;
        this.auditLogicService = auditLogicService;
        this.notificationPublisher = notificationPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public DefaultResult<List<FmRequestTrackView>> mine(String tenantId)
            throws ServiceException {
        String account = currentAccount(tenantId);
        Map<String, FmProcessInstance> processes = new LinkedHashMap<>();
        Map<String, Object> starterParameters = new HashMap<>();
        starterParameters.put("tenantId", tenantId);
        starterParameters.put("initiatorAccount", account);
        processInstanceService.selectListByParams(starterParameters).getValue()
                .forEach(value -> processes.put(value.getProcessInstanceId(), value));

        Map<String, Object> ownerParameters = new HashMap<>();
        ownerParameters.put("tenantId", tenantId);
        ownerParameters.put("ownerAccount", account);
        for (FmFormData formData : formDataService
                .selectListByParams(ownerParameters).getValue()) {
            Map<String, Object> processParameters = new HashMap<>();
            processParameters.put("tenantId", tenantId);
            processParameters.put("formDataId", formData.getFormDataId());
            processInstanceService.selectListByParams(processParameters).getValue()
                    .forEach(value -> processes.put(
                            value.getProcessInstanceId(), value));
        }
        List<FmRequestTrackView> values = new ArrayList<>();
        for (FmProcessInstance process : processes.values()) {
            values.add(trackView(process, requiredFormData(
                    tenantId, process.getFormDataId())));
        }
        values.sort(Comparator.comparing(
                FmRequestTrackView::startDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return success(List.copyOf(values));
    }

    @Override
    public DefaultResult<FmRequestTrackDetailView> load(
            String tenantId, String processInstanceId) throws ServiceException {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ServiceException("流程實例編號不可為空");
        }
        String account = currentAccount(tenantId);
        FmProcessInstance process = requiredProcess(tenantId, processInstanceId);
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        if (!account.equals(process.getInitiatorAccount())
                && !account.equals(formData.getOwnerAccount())) {
            throw new ServiceException("目前帳號無權查看此申請");
        }
        FmFormVersion formVersion = formVersion(
                tenantId, formData.getFormId(), formData.getFormVersionNo());
        return success(new FmRequestTrackDetailView(
                trackView(process, formData),
                formData.getFormId(),
                formData.getFormVersionNo(),
                formVersion.getSchemaContent(),
                formVersion.getUiSchemaContent(),
                formVersion.getCustomScriptContent(),
                parseData(formData.getDataContent()),
                currentApprovals(tenantId, processInstanceId),
                actions(tenantId, processInstanceId),
                snapshots(tenantId, processInstanceId)));
    }

    @Override
    public DefaultResult<FmRequestProcessDiagramView> diagram(
            String tenantId, String processInstanceId) throws ServiceException {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ServiceException("流程實例編號不可為空");
        }
        String account = currentAccount(tenantId);
        FmProcessInstance process = requiredProcess(tenantId, processInstanceId);
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        if (!canViewDiagram(tenantId, process, formData, account)) {
            throw new ServiceException("只有流程參與者可以查看流程進度");
        }
        Map<String, Object> versionParameters = new HashMap<>();
        versionParameters.put("tenantId", tenantId);
        versionParameters.put("processDefId", process.getProcessDefId());
        versionParameters.put("versionNo", process.getProcessVersionNo());
        FmProcessVersion version = processVersionService
                .selectListByParams(versionParameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到流程版本"));
        List<String> activeIds = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult() == null
                        ? List.of()
                        : runtimeService.getActiveActivityIds(processInstanceId)
                                .stream().distinct().toList();
        List<String> completedIds = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceStartTime().asc()
                .list().stream()
                .map(value -> value.getActivityId())
                .filter(StringUtils::isNotBlank)
                .distinct().toList();
        return success(new FmRequestProcessDiagramView(
                version.getBpmnXml(), process.getInstanceStatus(),
                activeIds, completedIds));
    }

    private boolean canViewDiagram(
            String tenantId,
            FmProcessInstance process,
            FmFormData formData,
            String account) {
        if (account.equals(process.getInitiatorAccount())
                || account.equals(formData.getOwnerAccount())) {
            return true;
        }
        boolean currentParticipant = taskService.createTaskQuery()
                .processInstanceId(process.getProcessInstanceId())
                .taskCandidateOrAssigned(account)
                .count() > 0;
        if (currentParticipant) {
            return true;
        }
        Map<String, Object> parameters = processParameters(
                tenantId, process.getProcessInstanceId());
        parameters.put("actorAccount", account);
        return !taskActionService.selectListByParams(parameters).getValue().isEmpty();
    }

    @Override
    @Transactional(readOnly = false)
    public DefaultResult<FmTaskActionResultView> withdraw(
            String tenantId, String processInstanceId, String reason)
            throws ServiceException {
        return terminateRequest(
                tenantId, processInstanceId, reason, "WITHDRAW", true);
    }

    @Override
    @Transactional(readOnly = false)
    public DefaultResult<FmTaskActionResultView> cancel(
            String tenantId, String processInstanceId, String reason)
            throws ServiceException {
        return terminateRequest(
                tenantId, processInstanceId, reason, "CANCEL", false);
    }

    private DefaultResult<FmTaskActionResultView> terminateRequest(
            String tenantId,
            String processInstanceId,
            String reason,
            String actionType,
            boolean ownerRequired) throws ServiceException {
        if (StringUtils.isAnyBlank(processInstanceId, reason)) {
            throw new ServiceException("流程實例與原因不可為空");
        }
        if (reason.trim().length() > 1000) {
            throw new ServiceException("原因不可超過 1000 字");
        }
        reason = reason.trim();
        String account = currentAccount(tenantId);
        FmProcessInstance process = requiredProcess(tenantId, processInstanceId);
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        formDataService.lockByFormDataId(tenantId, formData.getFormDataId());
        process = requiredProcess(tenantId, processInstanceId);
        formData = requiredFormData(tenantId, process.getFormDataId());
        String authorizedAccount = ownerRequired
                ? formData.getOwnerAccount() : process.getInitiatorAccount();
        if (!account.equals(authorizedAccount)) {
            throw new ServiceException(ownerRequired
                    ? "只有申請人可以撤回申請"
                    : "只有實際發起人可以取消流程");
        }
        if (!"RUNNING".equals(process.getInstanceStatus())) {
            throw new ServiceException("只有進行中的流程可以終止");
        }
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult() == null) {
            throw new ServiceException("流程執行個體不存在，無法終止");
        }
        Date now = new Date();
        auditLogicService.recordTaskAction(
                tenantId, processInstanceId, null, null,
                actionType, "CANCELLED", account, formData.getOwnerAccount(),
                null, reason, formData, null, now);
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        if (!processInstanceService.updateStatus(
                tenantId, processInstanceId, "RUNNING", "CANCELLED",
                now, account)) {
            throw new ServiceException("流程狀態已變更，請重新整理後再試");
        }
        formData.setDataStatus("CANCELLED");
        formData.setUuserid(account);
        formData.setUdate(now);
        formDataService.update(formData);
        notificationPublisher.processStatusChanged(
                tenantId, processInstanceId, "CANCELLED",
                List.of(formData.getOwnerAccount(), process.getInitiatorAccount()),
                account, now);
        return success(new FmTaskActionResultView(
                null, actionType, processInstanceId, "CANCELLED"));
    }

    private FmRequestTrackView trackView(
            FmProcessInstance process, FmFormData formData) throws ServiceException {
        FmProcessDef processDef = processDef(
                process.getTenantId(), process.getProcessDefId());
        FmFormDef formDef = formDef(process.getTenantId(), formData.getFormId());
        List<String> currentTasks = taskService.createTaskQuery()
                .processInstanceId(process.getProcessInstanceId()).list().stream()
                .map(value -> value.getName()).distinct().toList();
        return new FmRequestTrackView(
                process.getProcessInstanceId(),
                process.getBusinessKey(),
                process.getDocumentNumber(),
                processDef.getProcessName(),
                formDef.getFormName(),
                formData.getOwnerAccount(),
                process.getInitiatorAccount(),
                process.getInstanceStatus(),
                process.getStartDate(),
                process.getEndDate(),
                currentTasks);
    }

    private List<FmTaskActionView> actions(
            String tenantId, String processInstanceId) {
        Map<String, Object> parameters = processParameters(tenantId, processInstanceId);
        return taskActionService.selectListByParams(
                parameters, "ACTION_DATE", "ASC").getValue().stream()
                .map(this::actionView).toList();
    }

    private List<FmCurrentApprovalView> currentApprovals(
            String tenantId, String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime().asc().list().stream()
                .map(task -> currentApproval(tenantId, task))
                .toList();
    }

    private FmCurrentApprovalView currentApproval(String tenantId, Task task) {
        List<String> accounts;
        String assignmentType;
        if (StringUtils.isNotBlank(task.getAssignee())) {
            accounts = List.of(task.getAssignee());
            assignmentType = "ASSIGNEE";
        } else {
            accounts = taskService.getIdentityLinksForTask(task.getId()).stream()
                    .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()))
                    .map(link -> link.getUserId())
                    .filter(StringUtils::isNotBlank)
                    .distinct().toList();
            assignmentType = "CANDIDATE";
        }
        return new FmCurrentApprovalView(
                task.getId(), task.getTaskDefinitionKey(), task.getName(),
                assignmentType,
                accounts.stream().map(account -> approver(tenantId, account)).toList(),
                task.getCreateTime());
    }

    private FmCurrentApprovalPersonView approver(String tenantId, String account) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        String displayName = employeeService.selectListByParams(parameters)
                .getValue().stream().findFirst()
                .map(FmEmployee::getDisplayName).orElse(account);
        return new FmCurrentApprovalPersonView(account, displayName);
    }

    private List<FmFormSnapshotView> snapshots(
            String tenantId, String processInstanceId) throws ServiceException {
        Map<String, Object> parameters = processParameters(tenantId, processInstanceId);
        List<FmFormSnapshotView> values = new ArrayList<>();
        for (FmFormSnapshot snapshot : formSnapshotService.selectListByParams(
                parameters, "SNAPSHOT_DATE", "ASC").getValue()) {
            values.add(new FmFormSnapshotView(
                    snapshot.getFormSnapshotId(),
                    snapshot.getTaskId(),
                    snapshot.getActionType(),
                    snapshot.getRevisionNo(),
                    snapshot.getContentSha256(),
                    snapshot.getSnapshotDate(),
                    parseData(snapshot.getDataContent())));
        }
        return List.copyOf(values);
    }

    private FmTaskActionView actionView(FmTaskAction action) {
        return new FmTaskActionView(
                action.getActionType(), action.getOutcome(),
                action.getActorAccount(), action.getCommentText(),
                action.getReason(), action.getActionDate());
    }

    private FmProcessInstance requiredProcess(
            String tenantId, String processInstanceId) throws ServiceException {
        Map<String, Object> parameters = processParameters(tenantId, processInstanceId);
        return processInstanceService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到流程實例"));
    }

    private FmFormData requiredFormData(String tenantId, String formDataId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formDataId", formDataId);
        return formDataService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到申請表單資料"));
    }

    private FmProcessDef processDef(String tenantId, String processDefId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        return processDefService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到流程定義"));
    }

    private FmFormDef formDef(String tenantId, String formId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        return formDefService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到表單主檔"));
    }

    private FmFormVersion formVersion(
            String tenantId, String formId, Integer versionNo) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        parameters.put("versionNo", versionNo);
        return formVersionService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到表單版本"));
    }

    private String currentAccount(String tenantId) throws ServiceException {
        String account = UserUtils.getCurrentUser().getUsername();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        Date now = new Date();
        boolean active = tenantAccountService.selectListByParams(parameters)
                .getValue().stream().anyMatch(value -> effective(value, now));
        if (!active) {
            throw new ServiceException("目前帳號不屬於指定 Tenant");
        }
        return account;
    }

    private boolean effective(FmTenantAccount value, Date now) {
        return (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
                && (value.getEffectiveTo() == null || value.getEffectiveTo().after(now));
    }

    private Map<String, Object> parseData(String content) throws ServiceException {
        try {
            return objectMapper.readValue(
                    content, new TypeReference<Map<String, Object>>() { });
        } catch (RuntimeException exception) {
            throw new ServiceException("表單資料 JSON 格式不正確");
        }
    }

    private Map<String, Object> processParameters(
            String tenantId, String processInstanceId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processInstanceId", processInstanceId);
        return parameters;
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
