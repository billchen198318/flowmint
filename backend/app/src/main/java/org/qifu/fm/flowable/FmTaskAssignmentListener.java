package org.qifu.fm.flowable;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.resolver.IFmAssignmentResolverService;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.dto.command.FmAssignmentSnapshotCommand;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component("fmTaskAssignmentListener")
public class FmTaskAssignmentListener implements TaskListener {

    public static final String VARIABLE_TENANT_ID = "flowmintTenantId";
    public static final String VARIABLE_PROCESS_DEF_ID = "flowmintProcessDefId";
    public static final String VARIABLE_PROCESS_VERSION_NO = "flowmintProcessVersionNo";
    public static final String VARIABLE_INITIATOR_ACCOUNT = "flowmintInitiatorAccount";
    public static final String VARIABLE_INITIATOR_ORG_UNIT_ID =
            "flowmintInitiatorOrgUnitId";
    public static final String VARIABLE_FORM_DATA = "flowmintFormData";
    public static final String VARIABLE_FORM_DATA_ID = "flowmintFormDataId";

    private final IFmTaskAssignmentRuleService assignmentRuleService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmAssignmentResolverService assignmentResolverService;
    private final IFmRuntimeAuditLogicService runtimeAuditService;
    private final ObjectMapper objectMapper;
    private final FmAssignmentIncidentRecorder incidentRecorder;
    private final FmNotificationPublisher notificationPublisher;

    public FmTaskAssignmentListener(
            IFmTaskAssignmentRuleService assignmentRuleService,
            IFmTaskPolicyService taskPolicyService,
            IFmAssignmentResolverService assignmentResolverService,
            IFmRuntimeAuditLogicService runtimeAuditService,
            FmAssignmentIncidentRecorder incidentRecorder,
            FmNotificationPublisher notificationPublisher,
            ObjectMapper objectMapper) {
        this.assignmentRuleService = assignmentRuleService;
        this.taskPolicyService = taskPolicyService;
        this.assignmentResolverService = assignmentResolverService;
        this.runtimeAuditService = runtimeAuditService;
        this.incidentRecorder = incidentRecorder;
        this.notificationPublisher = notificationPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(DelegateTask task) {
        try {
            RuntimeContext context = context(task);
            FmTaskPolicy policy = policy(context, task.getTaskDefinitionKey());
            applyDueDate(task, policy);
            if ("APPLICANT_CORRECTION".equals(policy.getAssignmentMode())) {
                Date now = new Date();
                task.setAssignee(context.initiatorAccount());
                runtimeAuditService.recordAssignmentSnapshot(
                        new FmAssignmentSnapshotCommand(
                                context.tenantId(), context.formDataId(),
                                task.getProcessInstanceId(), task.getId(),
                                task.getTaskDefinitionKey(), "APPLICANT",
                                context.initiatorAccount(), context.initiatorOrgUnitId(),
                                "{\"source\":\"FORM_OWNER\"}", "ASSIGNEE", List.of()),
                        now);
                notificationPublisher.taskAssigned(
                        context.tenantId(), task.getId(), task.getName(),
                        List.of(context.initiatorAccount()),
                        context.initiatorAccount(), now);
                return;
            }
            ResolvedAssignment resolved = resolveAssignment(
                    context,
                    task.getTaskDefinitionKey());
            applyAssignment(task, policy, resolved.accounts());
            Date now = new Date();
            runtimeAuditService.recordAssignmentSnapshot(
                    new FmAssignmentSnapshotCommand(
                            context.tenantId(),
                            context.formDataId(),
                            task.getProcessInstanceId(),
                            task.getId(),
                            task.getTaskDefinitionKey(),
                            resolved.resolverType(),
                            context.initiatorAccount(),
                            context.initiatorOrgUnitId(),
                            resolved.resolutionContext(),
                            "CANDIDATE".equals(policy.getAssignmentMode())
                                    ? "CANDIDATE" : "ASSIGNEE",
                            resolved.candidates()),
                    now);
            notificationPublisher.taskAssigned(
                    context.tenantId(), task.getId(), task.getName(),
                    resolved.accounts(), context.initiatorAccount(), now);
        } catch (ServiceException exception) {
            RuntimeContext context = context(task);
            String incidentId = incidentRecorder.record(
                    new FmAssignmentIncidentRecorder.IncidentCommand(
                            context.tenantId(), task.getProcessInstanceId(), task.getId(),
                            task.getTaskDefinitionKey(), "ASSIGNMENT",
                            "RESOLVER_FAILED", exception.getMessage(),
                            objectMapper.writeValueAsString(Map.of(
                                    "processDefId", context.processDefId(),
                                    "processVersionNo", context.versionNo(),
                                    "initiatorAccount", context.initiatorAccount())),
                            context.initiatorAccount(), new Date()));
            task.setCategory("FLOWMINT_INCIDENT:" + incidentId);
        }
    }

    public List<String> multiInstanceAccounts(
            DelegateExecution execution, String taskDefKey) {
        try {
            return List.copyOf(resolveAssignment(context(execution), taskDefKey).accounts());
        } catch (ServiceException exception) {
            throw new FlowableException("FlowMint Multi-instance 指派失敗："
                    + exception.getMessage(), exception);
        }
    }

    private RuntimeContext context(DelegateTask task) throws ServiceException {
        String tenantId = stringVariable(task, VARIABLE_TENANT_ID);
        String processDefId = stringVariable(task, VARIABLE_PROCESS_DEF_ID);
        String initiatorAccount = stringVariable(task, VARIABLE_INITIATOR_ACCOUNT);
        String initiatorOrgUnitId = stringVariable(task, VARIABLE_INITIATOR_ORG_UNIT_ID);
        String formDataId = stringVariable(task, VARIABLE_FORM_DATA_ID);
        Object versionValue = task.getVariable(VARIABLE_PROCESS_VERSION_NO);
        if (StringUtils.isAnyBlank(
                tenantId, processDefId, initiatorAccount, formDataId)
                || versionValue == null) {
            throw new ServiceException("流程缺少 FlowMint Runtime 必要變數");
        }
        Integer versionNo;
        try {
            versionNo = Integer.valueOf(versionValue.toString());
        } catch (NumberFormatException exception) {
            throw new ServiceException("流程版本變數格式不正確");
        }
        Object formValue = task.getVariable(VARIABLE_FORM_DATA);
        Map<String, Object> formData = formValue instanceof Map<?, ?> map
                ? stringKeyMap(map) : Map.of();
        return new RuntimeContext(
                tenantId,
                processDefId,
                versionNo,
                initiatorAccount,
                initiatorOrgUnitId,
                formDataId,
                Map.of("form", formData));
    }

    private RuntimeContext context(DelegateExecution execution) throws ServiceException {
        String tenantId = stringVariable(execution, VARIABLE_TENANT_ID);
        String processDefId = stringVariable(execution, VARIABLE_PROCESS_DEF_ID);
        String initiatorAccount = stringVariable(execution, VARIABLE_INITIATOR_ACCOUNT);
        String initiatorOrgUnitId = stringVariable(
                execution, VARIABLE_INITIATOR_ORG_UNIT_ID);
        String formDataId = stringVariable(execution, VARIABLE_FORM_DATA_ID);
        Object versionValue = execution.getVariable(VARIABLE_PROCESS_VERSION_NO);
        if (StringUtils.isAnyBlank(tenantId, processDefId, initiatorAccount, formDataId)
                || versionValue == null) {
            throw new ServiceException("流程缺少 FlowMint Runtime 標準變數");
        }
        Integer versionNo;
        try {
            versionNo = Integer.valueOf(versionValue.toString());
        } catch (NumberFormatException exception) {
            throw new ServiceException("流程版本變數格式不正確");
        }
        Object formValue = execution.getVariable(VARIABLE_FORM_DATA);
        Map<String, Object> formData = formValue instanceof Map<?, ?> map
                ? stringKeyMap(map) : Map.of();
        return new RuntimeContext(tenantId, processDefId, versionNo,
                initiatorAccount, initiatorOrgUnitId, formDataId,
                Map.of("form", formData));
    }

    private FmTaskPolicy policy(RuntimeContext context, String taskDefKey)
            throws ServiceException {
        return taskPolicyService.findByVersion(
                context.tenantId(),
                context.processDefId(),
                context.versionNo()).stream()
                .filter(value -> taskDefKey.equals(value.getTaskDefKey()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到 User Task 派送政策"));
    }

    private ResolvedAssignment resolveAssignment(RuntimeContext context, String taskDefKey)
            throws ServiceException {
        List<FmTaskAssignmentRule> rules = assignmentRuleService.findByVersion(
                context.tenantId(),
                context.processDefId(),
                context.versionNo()).stream()
                .filter(value -> taskDefKey.equals(value.getTaskDefKey()))
                .filter(value -> "ACTIVE".equals(value.getStatus()))
                .toList();
        if (rules.isEmpty()) {
            throw new ServiceException("找不到啟用中的簽核人規則");
        }
        Set<String> accounts = new LinkedHashSet<>();
        Map<String, FmResolverCandidateView> candidates = new java.util.LinkedHashMap<>();
        List<Map<String, Object>> resolutionPath = new ArrayList<>();
        for (FmTaskAssignmentRule rule : rules) {
            FmResolverPreviewView resolved = assignmentResolverService.resolve(
                    rule,
                    context.initiatorAccount(),
                    context.variables());
            if (!"RESOLVED".equals(resolved.resultStatus())) {
                throw new ServiceException(resolved.message());
            }
            for (FmResolverCandidateView candidate : resolved.candidates()) {
                if (StringUtils.isNotBlank(candidate.account())) {
                    accounts.add(candidate.account());
                    candidates.putIfAbsent(candidate.account(), candidate);
                }
            }
            Map<String, Object> pathItem = new java.util.LinkedHashMap<>();
            pathItem.put("ruleSeq", rule.getRuleSeq());
            pathItem.put("resolverType", rule.getResolverType());
            pathItem.put("accounts", resolved.candidates().stream()
                    .map(FmResolverCandidateView::account).toList());
            resolutionPath.add(pathItem);
        }
        if (accounts.isEmpty()) {
            throw new ServiceException("簽核人規則沒有解析出有效帳號");
        }
        String resolverType = rules.size() == 1
                ? rules.getFirst().getResolverType() : "COMPOSITE";
        return new ResolvedAssignment(
                accounts,
                List.copyOf(candidates.values()),
                resolverType,
                objectMapper.writeValueAsString(resolutionPath));
    }

    private void applyAssignment(
            DelegateTask task,
            FmTaskPolicy policy,
            Set<String> accounts) throws ServiceException {
        switch (policy.getAssignmentMode()) {
            case "ASSIGNEE" -> task.setAssignee(accounts.iterator().next());
            case "CANDIDATE" -> task.addCandidateUsers(accounts);
            case "ALL", "SEQUENTIAL" -> {
                Object current = task.getVariable("flowmintAssignee");
                String account = current == null ? null : current.toString();
                if (StringUtils.isBlank(account) || !accounts.contains(account)) {
                    throw new ServiceException("Multi-instance 簽核人變數不正確");
                }
                task.setAssignee(account);
            }
            default -> throw new ServiceException("不支援的 User Task 派送方式");
        }
    }

    private void applyDueDate(DelegateTask task, FmTaskPolicy policy) {
        if (policy.getDueHours() != null) {
            task.setDueDate(Date.from(java.time.Instant.now().plus(
                    policy.getDueHours(), java.time.temporal.ChronoUnit.HOURS)));
        }
    }

    private String stringVariable(DelegateTask task, String name) {
        Object value = task.getVariable(name);
        return value == null ? null : value.toString();
    }

    private String stringVariable(DelegateExecution execution, String name) {
        Object value = execution.getVariable(name);
        return value == null ? null : value.toString();
    }

    private Map<String, Object> stringKeyMap(Map<?, ?> source) {
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private record RuntimeContext(
            String tenantId,
            String processDefId,
            Integer versionNo,
            String initiatorAccount,
            String initiatorOrgUnitId,
            String formDataId,
            Map<String, Object> variables) {
    }

    private record ResolvedAssignment(
            Set<String> accounts,
            List<FmResolverCandidateView> candidates,
            String resolverType,
            String resolutionContext) {
    }
}
