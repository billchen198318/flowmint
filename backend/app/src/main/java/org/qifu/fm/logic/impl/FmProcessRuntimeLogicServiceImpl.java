package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.domain.runtime.FmProcessStartPolicyEvaluator;
import org.qifu.fm.domain.runtime.FmProcessStartPolicyEvaluator.StartSubject;
import org.qifu.fm.domain.runtime.FmProcessStartProxyEvaluator;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.entity.FmApprovalGroupMember;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmApprovalGroupMemberService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmProcessStartPolicyService;
import org.qifu.fm.service.IFmProcessStartProxyService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmProcessRuntimeLogicServiceImpl
        implements IFmProcessRuntimeLogicService {

    private final IFmProcessVersionService processVersionService;
    private final IFmFormVersionService formVersionService;
    private final IFmEmployeeService employeeService;
    private final IFmEmployeeOrgAssignmentService assignmentService;
    private final IFmFormDataService formDataService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmProcessStartPolicyService startPolicyService;
    private final IFmApprovalGroupMemberService approvalGroupMemberService;
    private final FmProcessStartPolicyEvaluator startPolicyEvaluator;
    private final IFmProcessStartProxyService startProxyService;
    private final FmProcessStartProxyEvaluator startProxyEvaluator;
    private final IFmTaskFormRuleService taskFormRuleService;
    private final FmFormSubmissionValidator formSubmissionValidator;
    private final IFmRuntimeAuditLogicService runtimeAuditService;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    public FmProcessRuntimeLogicServiceImpl(
            IFmProcessVersionService processVersionService,
            IFmFormVersionService formVersionService,
            IFmEmployeeService employeeService,
            IFmEmployeeOrgAssignmentService assignmentService,
            IFmFormDataService formDataService,
            IFmProcessInstanceService processInstanceService,
            IFmProcessStartPolicyService startPolicyService,
            IFmApprovalGroupMemberService approvalGroupMemberService,
            FmProcessStartPolicyEvaluator startPolicyEvaluator,
            IFmProcessStartProxyService startProxyService,
            FmProcessStartProxyEvaluator startProxyEvaluator,
            IFmTaskFormRuleService taskFormRuleService,
            FmFormSubmissionValidator formSubmissionValidator,
            IFmRuntimeAuditLogicService runtimeAuditService,
            RuntimeService runtimeService,
            ObjectMapper objectMapper) {
        this.processVersionService = processVersionService;
        this.formVersionService = formVersionService;
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
        this.formDataService = formDataService;
        this.processInstanceService = processInstanceService;
        this.startPolicyService = startPolicyService;
        this.approvalGroupMemberService = approvalGroupMemberService;
        this.startPolicyEvaluator = startPolicyEvaluator;
        this.startProxyService = startProxyService;
        this.startProxyEvaluator = startProxyEvaluator;
        this.taskFormRuleService = taskFormRuleService;
        this.formSubmissionValidator = formSubmissionValidator;
        this.runtimeAuditService = runtimeAuditService;
        this.runtimeService = runtimeService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessSubmitView> submit(FmProcessSubmitCommand command)
            throws ServiceException {
        validate(command);
        FmProcessVersion processVersion = publishedProcessVersion(command);
        FmFormVersion formVersion = publishedFormVersion(command);
        ensureFormBound(command, processVersion);
        formSubmissionValidator.validate(formVersion.getSchemaContent(), command.formData());
        FmEmployee applicant = activeApplicant(command.tenantId(), command.applicantAccount());
        String starterAccount = UserUtils.getCurrentUser().getUsername();
        activeApplicant(command.tenantId(), starterAccount);
        authorizeProxy(command, starterAccount);
        FmEmployeeOrgAssignment assignment = primaryAssignment(
                command.tenantId(),
                applicant.getEmployeeId());
        authorizeStart(command, processVersion, applicant);
        String businessKey = StringUtils.defaultIfBlank(
                command.businessKey(),
                UUID.randomUUID().toString());
        ensureBusinessKeyAvailable(command.tenantId(), businessKey);
        Date now = new Date();
        FmFormData formData = insertFormData(
                command,
                assignment,
                businessKey,
                now);
        ProcessInstance flowableInstance = runtimeService.startProcessInstanceById(
                processVersion.getFlowableProcessDefId(),
                businessKey,
                runtimeVariables(
                        command, processVersion, assignment, formData, businessKey));
        FmProcessInstance processInstance = insertProcessInstance(
                command,
                processVersion,
                assignment,
                formData,
                flowableInstance,
                businessKey,
                starterAccount,
                now);
        runtimeAuditService.recordSubmit(
                command.tenantId(),
                processInstance.getProcessInstanceId(),
                formData,
                starterAccount,
                command.applicantAccount(),
                now);
        return success(new FmProcessSubmitView(
                businessKey,
                formData.getFormDataId(),
                processInstance.getProcessInstanceId(),
                processInstance.getInstanceStatus()));
    }

    private void validate(FmProcessSubmitCommand command) throws ServiceException {
        if (command == null
                || StringUtils.isAnyBlank(
                        command.tenantId(),
                        command.processDefId(),
                        command.formId(),
                        command.applicantAccount())
                || command.formVersionNo() == null
                || command.formData() == null) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
    }

    private FmProcessVersion publishedProcessVersion(FmProcessSubmitCommand command)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", command.tenantId());
        parameters.put("processDefId", command.processDefId());
        parameters.put("versionStatus", "PUBLISHED");
        FmProcessVersion version = processVersionService
                .selectListByParams(parameters, "VERSION_NO", "DESC")
                .getValue().stream().findFirst().orElse(null);
        if (version == null || StringUtils.isBlank(version.getFlowableProcessDefId())) {
            throw new ServiceException("流程尚未發布或缺少 Flowable 部署資訊");
        }
        return version;
    }

    private FmFormVersion publishedFormVersion(FmProcessSubmitCommand command)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", command.tenantId());
        parameters.put("formId", command.formId());
        parameters.put("versionNo", command.formVersionNo());
        parameters.put("versionStatus", "PUBLISHED");
        return formVersionService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("指定表單版本尚未發布"));
    }

    private void ensureFormBound(
            FmProcessSubmitCommand command,
            FmProcessVersion processVersion) throws ServiceException {
        boolean bound = taskFormRuleService.findByVersion(
                command.tenantId(),
                command.processDefId(),
                processVersion.getVersionNo()).stream()
                .anyMatch(rule -> command.formId().equals(rule.getFormId())
                        && command.formVersionNo().equals(rule.getFormVersionNo()));
        if (!bound) {
            throw new ServiceException(
                    "送出的表單版本未綁定至指定的流程版本");
        }
    }

    private FmEmployee activeApplicant(String tenantId, String account)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("account", account);
        return employeeService.selectListByParams(parameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("申請人不存在、未啟用或不在有效期間"));
    }

    private FmEmployeeOrgAssignment primaryAssignment(String tenantId, String employeeId)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", employeeId);
        parameters.put("isPrimary", "Y");
        return assignmentService.selectListByParams(parameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("申請人沒有有效的主要任職配置"));
    }

    private void authorizeStart(
            FmProcessSubmitCommand command,
            FmProcessVersion processVersion,
            FmEmployee applicant) throws ServiceException {
        Map<String, Object> assignmentParameters = activeParameters(command.tenantId());
        assignmentParameters.put("employeeId", applicant.getEmployeeId());
        Set<String> orgUnitIds = assignmentService
                .selectListByParams(assignmentParameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .map(FmEmployeeOrgAssignment::getOrgUnitId)
                .collect(Collectors.toSet());

        Map<String, Object> groupParameters = activeParameters(command.tenantId());
        groupParameters.put("employeeId", applicant.getEmployeeId());
        Set<String> groupIds = approvalGroupMemberService
                .selectListByParams(groupParameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .map(FmApprovalGroupMember::getApprovalGroupId)
                .collect(Collectors.toSet());

        boolean allowed = startPolicyEvaluator.isAllowed(
                startPolicyService.findByVersion(
                        command.tenantId(),
                        command.processDefId(),
                        processVersion.getVersionNo()),
                new StartSubject(applicant.getAccount(), orgUnitIds, groupIds));
        if (!allowed) {
            throw new ServiceException("申請人沒有此流程的起單權限");
        }
    }

    private void authorizeProxy(FmProcessSubmitCommand command, String starterAccount)
            throws ServiceException {
        if (starterAccount.equals(command.applicantAccount())) {
            return;
        }
        Map<String, Object> parameters = activeParameters(command.tenantId());
        parameters.put("principalAccount", command.applicantAccount());
        parameters.put("proxyAccount", starterAccount);
        boolean authorized = startProxyEvaluator.isAuthorized(
                starterAccount,
                command.applicantAccount(),
                command.processDefId(),
                startProxyService.selectListByParams(parameters).getValue(),
                new Date());
        if (!authorized) {
            throw new ServiceException("目前登入者沒有替此申請人代起單的授權");
        }
    }

    private void ensureBusinessKeyAvailable(String tenantId, String businessKey)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("businessKey", businessKey);
        if (!formDataService.selectListByParams(parameters).getValue().isEmpty()
                || !processInstanceService.selectListByParams(parameters).getValue().isEmpty()) {
            throw new ServiceException("Business key 已存在");
        }
    }

    private FmFormData insertFormData(
            FmProcessSubmitCommand command,
            FmEmployeeOrgAssignment assignment,
            String businessKey,
            Date now) {
        FmFormData value = new FmFormData();
        value.setTenantId(command.tenantId());
        value.setFormDataId(UUID.randomUUID().toString());
        value.setFormId(command.formId());
        value.setFormVersionNo(command.formVersionNo());
        value.setBusinessKey(businessKey);
        value.setOwnerAccount(command.applicantAccount());
        value.setOwnerOrgUnitId(assignment.getOrgUnitId());
        value.setDataContent(objectMapper.writeValueAsString(command.formData()));
        value.setDataStatus("SUBMITTED");
        value.setRevisionNo(1);
        value.setLockVersion(0);
        value.setSubmittedDate(now);
        formDataService.insert(value);
        return value;
    }

    private Map<String, Object> runtimeVariables(
            FmProcessSubmitCommand command,
            FmProcessVersion processVersion,
            FmEmployeeOrgAssignment assignment,
            FmFormData formData,
            String businessKey) {
        Map<String, Object> variables = new HashMap<>();
        variables.put(FmTaskAssignmentListener.VARIABLE_TENANT_ID, command.tenantId());
        variables.put(FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID,
                command.processDefId());
        variables.put(FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO,
                processVersion.getVersionNo());
        variables.put(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT,
                command.applicantAccount());
        variables.put(FmTaskAssignmentListener.VARIABLE_INITIATOR_ORG_UNIT_ID,
                assignment.getOrgUnitId());
        variables.put(FmTaskAssignmentListener.VARIABLE_FORM_DATA, command.formData());
        variables.put(FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID,
                formData.getFormDataId());
        variables.put("flowmintBusinessKey", businessKey);
        variables.put("flowmintStarterAccount",
                UserUtils.getCurrentUser().getUsername());
        return variables;
    }

    private FmProcessInstance insertProcessInstance(
            FmProcessSubmitCommand command,
            FmProcessVersion processVersion,
            FmEmployeeOrgAssignment assignment,
            FmFormData formData,
            ProcessInstance flowableInstance,
            String businessKey,
            String starterAccount,
            Date now) {
        FmProcessInstance value = new FmProcessInstance();
        value.setTenantId(command.tenantId());
        value.setProcessInstanceId(flowableInstance.getId());
        value.setProcessDefId(command.processDefId());
        value.setProcessVersionNo(processVersion.getVersionNo());
        value.setFlowableProcessDefId(processVersion.getFlowableProcessDefId());
        value.setBusinessKey(businessKey);
        value.setFormDataId(formData.getFormDataId());
        value.setInitiatorAccount(starterAccount);
        value.setInitiatorOrgUnitId(assignment.getOrgUnitId());
        value.setInstanceStatus("RUNNING");
        value.setStartDate(now);
        processInstanceService.insert(value);
        return value;
    }

    private Map<String, Object> activeParameters(String tenantId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return parameters;
    }

    private boolean isEffective(Date from, Date to) {
        Date now = new Date();
        return (from == null || !from.after(now)) && (to == null || to.after(now));
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
