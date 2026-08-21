package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.qifu.fm.domain.runtime.FmDocumentNumberService;
import org.qifu.fm.domain.attachment.FmAttachmentBindingService;
import org.qifu.fm.domain.runtime.FmProcessStartPolicyEvaluator;
import org.qifu.fm.domain.runtime.FmSystemFormFields;
import org.qifu.fm.domain.runtime.FmProcessStartPolicyEvaluator.StartSubject;
import org.qifu.fm.domain.runtime.FmProcessStartProxyEvaluator;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.command.FmProcessStartCatalogCommand;
import org.qifu.fm.dto.command.FmProcessStartLoadCommand;
import org.qifu.fm.dto.view.FmProcessStartCatalogView;
import org.qifu.fm.dto.view.FmProcessStartFormView;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.dto.view.FmRuntimeTenantView;
import org.qifu.fm.entity.FmApprovalGroupMember;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmApprovalGroupMemberService;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessStartPolicyService;
import org.qifu.fm.service.IFmProcessStartProxyService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmProcessRuntimeLogicServiceImpl
        implements IFmProcessRuntimeLogicService {

    private final IFmProcessVersionService processVersionService;
    private final IFmProcessDefService processDefService;
    private final IFmFormVersionService formVersionService;
    private final IFmFormDefService formDefService;
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
    private final IFmTenantAccountService tenantAccountService;
    private final IFmTenantService tenantService;
    private final FmFormSubmissionValidator formSubmissionValidator;
    private final IFmRuntimeAuditLogicService runtimeAuditService;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;
    private final FmAttachmentBindingService attachmentBindingService;
    private final FmDocumentNumberService documentNumberService;

    public FmProcessRuntimeLogicServiceImpl(
            IFmProcessVersionService processVersionService,
            IFmProcessDefService processDefService,
            IFmFormVersionService formVersionService,
            IFmFormDefService formDefService,
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
            IFmTenantAccountService tenantAccountService,
            IFmTenantService tenantService,
            FmFormSubmissionValidator formSubmissionValidator,
            IFmRuntimeAuditLogicService runtimeAuditService,
            RuntimeService runtimeService,
            ObjectMapper objectMapper,
            FmAttachmentBindingService attachmentBindingService,
            FmDocumentNumberService documentNumberService) {
        this.processVersionService = processVersionService;
        this.processDefService = processDefService;
        this.formVersionService = formVersionService;
        this.formDefService = formDefService;
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
        this.tenantAccountService = tenantAccountService;
        this.tenantService = tenantService;
        this.formSubmissionValidator = formSubmissionValidator;
        this.runtimeAuditService = runtimeAuditService;
        this.runtimeService = runtimeService;
        this.objectMapper = objectMapper;
        this.attachmentBindingService = attachmentBindingService;
        this.documentNumberService = documentNumberService;
    }

    @Override
    public DefaultResult<List<FmRuntimeTenantView>> tenants()
            throws ServiceException {
        String account = UserUtils.getCurrentUser().getUsername();
        Map<String, Object> membershipParameters = new HashMap<>();
        membershipParameters.put("account", account);
        membershipParameters.put("status", "ACTIVE");
        Map<String, FmTenantAccount> memberships = tenantAccountService
                .selectListByParams(membershipParameters).getValue().stream()
                .filter(value -> isEffective(
                        value.getEffectiveFrom(), value.getEffectiveTo()))
                .collect(Collectors.toMap(
                        FmTenantAccount::getTenantId,
                        value -> value,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        if (memberships.isEmpty()) {
            return success(List.of());
        }
        Map<String, Object> tenantParameters = new HashMap<>();
        tenantParameters.put("status", "ACTIVE");
        List<FmRuntimeTenantView> values = tenantService
                .selectListByParams(tenantParameters, "TENANT_NAME", "ASC")
                .getValue().stream()
                .filter(value -> memberships.containsKey(value.getTenantId()))
                .map(value -> tenantView(value, memberships.get(value.getTenantId())))
                .toList();
        return success(values);
    }

    @Override
    public DefaultResult<List<FmProcessStartCatalogView>> catalog(
            FmProcessStartCatalogCommand command) throws ServiceException {
        if (command == null || StringUtils.isAnyBlank(
                command.tenantId(), command.applicantAccount())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        String starterAccount = UserUtils.getCurrentUser().getUsername();
        validateTenantMembership(command.tenantId(), starterAccount);
        activeApplicant(command.tenantId(), starterAccount);
        FmEmployee applicant = activeApplicant(
                command.tenantId(), command.applicantAccount());
        Map<String, Object> parameters = publishedParameters(command.tenantId());
        List<FmProcessStartCatalogView> values = new ArrayList<>();
        for (FmProcessDef processDef : processDefService
                .selectListByParams(parameters, "PROCESS_NAME", "ASC").getValue()) {
            try {
                FmProcessVersion version = publishedProcessVersion(
                        command.tenantId(), processDef.getProcessDefId());
                authorizeProxy(
                        command.tenantId(), processDef.getProcessDefId(),
                        command.applicantAccount(), starterAccount);
                authorizeStart(
                        command.tenantId(), processDef.getProcessDefId(),
                        version, applicant);
                startForms(command.tenantId(), version);
                values.add(new FmProcessStartCatalogView(
                        processDef.getProcessDefId(),
                        processDef.getProcessKey(),
                        processDef.getProcessName(),
                        processDef.getCategory(),
                        processDef.getDescription(),
                        version.getVersionNo()));
            } catch (ServiceException ignored) {
                // Catalog only exposes processes that are currently startable.
            }
        }
        return success(List.copyOf(values));
    }

    private FmRuntimeTenantView tenantView(
            FmTenant tenant, FmTenantAccount membership) {
        return new FmRuntimeTenantView(
                tenant.getTenantId(),
                tenant.getTenantCode(),
                tenant.getTenantName(),
                "Y".equals(membership.getIsDefault()));
    }

    @Override
    public DefaultResult<FmProcessStartLoadView> loadStart(FmProcessStartLoadCommand command)
            throws ServiceException {
        if (command == null || StringUtils.isAnyBlank(
                command.tenantId(), command.processDefId(), command.applicantAccount())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        String starterAccount = UserUtils.getCurrentUser().getUsername();
        validateTenantMembership(command.tenantId(), starterAccount);
        FmProcessVersion processVersion = publishedProcessVersion(
                command.tenantId(), command.processDefId());
        FmProcessDef processDef = activeProcessDef(
                command.tenantId(), command.processDefId());
        FmEmployee applicant = activeApplicant(
                command.tenantId(), command.applicantAccount());
        activeApplicant(command.tenantId(), starterAccount);
        authorizeProxy(
                command.tenantId(), command.processDefId(),
                command.applicantAccount(), starterAccount);
        authorizeStart(
                command.tenantId(), command.processDefId(),
                processVersion, applicant);
        return success(new FmProcessStartLoadView(
                command.processDefId(),
                processVersion.getVersionNo(),
                processDef.getProcessKey(),
                processDef.getProcessName(),
                command.applicantAccount(),
                startForms(command.tenantId(), processVersion)));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessSubmitView> submit(FmProcessSubmitCommand command)
            throws ServiceException {
        validate(command);
        FmProcessVersion processVersion = publishedProcessVersion(
                command.tenantId(), command.processDefId());
        FmFormVersion formVersion = publishedFormVersion(command);
        ensureFormBound(command, processVersion);
        formSubmissionValidator.validate(formVersion.getSchemaContent(), command.formData());
        FmEmployee applicant = activeApplicant(command.tenantId(), command.applicantAccount());
        String starterAccount = UserUtils.getCurrentUser().getUsername();
        validateTenantMembership(command.tenantId(), starterAccount);
        activeApplicant(command.tenantId(), starterAccount);
        authorizeProxy(
                command.tenantId(), command.processDefId(),
                command.applicantAccount(), starterAccount);
        FmEmployeeOrgAssignment assignment = selectedOrPrimaryAssignment(
                command.tenantId(),
                applicant,
                command.formData());
        authorizeStart(
                command.tenantId(), command.processDefId(),
                processVersion, applicant);
        FmProcessSubmitView existing = existingSubmission(command, starterAccount);
        if (existing != null) {
            return success(existing);
        }
        FmProcessDef processDef = activeProcessDef(
                command.tenantId(), command.processDefId());
        Date now = new Date();
        String businessKey = UUID.randomUUID().toString();
        String documentNumber = nextDocumentNumber(
                processDef, command.tenantId(), starterAccount, now);
        FmFormData formData = insertFormData(
                command,
                assignment,
                businessKey,
                documentNumber,
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
                documentNumber,
                starterAccount,
                now);
        runtimeAuditService.recordSubmit(
                command.tenantId(),
                processInstance.getProcessInstanceId(),
                formData,
                starterAccount,
                command.applicantAccount(),
                now);
        attachmentBindingService.bind(
                command.tenantId(), command.uploadSessionId(), starterAccount,
                command.formId(), command.formVersionNo(), formData.getFormDataId(), now);
        return success(new FmProcessSubmitView(
                businessKey,
                documentNumber,
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
                        command.idempotencyKey(),
                        command.applicantAccount())
                || command.formVersionNo() == null
                || command.formData() == null) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
    }

    private FmProcessVersion publishedProcessVersion(String tenantId, String processDefId)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
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
        return publishedFormVersion(
                command.tenantId(), command.formId(), command.formVersionNo());
    }

    private FmFormVersion publishedFormVersion(
            String tenantId, String formId, Integer formVersionNo)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        parameters.put("versionNo", formVersionNo);
        parameters.put("versionStatus", "PUBLISHED");
        return formVersionService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("指定表單版本尚未發布"));
    }

    private FmProcessDef activeProcessDef(String tenantId, String processDefId)
            throws ServiceException {
        Map<String, Object> parameters = publishedParameters(tenantId);
        parameters.put("processDefId", processDefId);
        return processDefService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到可發起的流程主檔"));
    }

    private String nextDocumentNumber(
            FmProcessDef processDef, String tenantId, String account, Date now)
            throws ServiceException {
        if (StringUtils.isBlank(processDef.getDocumentType())) {
            return null;
        }
        Map<String, Object> parameters = activeParameters(tenantId);
        FmTenant tenant = tenantService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到啟用的 Tenant"));
        return documentNumberService.nextNumber(
                tenantId,
                tenant.getTenantCode(),
                tenant.getDefaultTimezone(),
                processDef.getDocumentType(),
                account,
                now);
    }

    private List<FmProcessStartFormView> startForms(
            String tenantId, FmProcessVersion processVersion) throws ServiceException {
        Map<String, List<String>> taskKeysByForm = new LinkedHashMap<>();
        Map<String, FmTaskFormRule> rulesByForm = new LinkedHashMap<>();
        for (FmTaskFormRule rule : taskFormRuleService.findByVersion(
                tenantId,
                processVersion.getProcessDefId(),
                processVersion.getVersionNo())) {
            String key = rule.getFormId() + ":" + rule.getFormVersionNo();
            rulesByForm.putIfAbsent(key, rule);
            taskKeysByForm.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(rule.getTaskDefKey());
        }
        if (rulesByForm.isEmpty()) {
            throw new ServiceException("已發布流程版本沒有綁定表單");
        }
        List<FmProcessStartFormView> forms = new ArrayList<>();
        for (Map.Entry<String, FmTaskFormRule> entry : rulesByForm.entrySet()) {
            FmTaskFormRule rule = entry.getValue();
            FmFormVersion version = publishedFormVersion(
                    tenantId, rule.getFormId(), rule.getFormVersionNo());
            FmFormDef formDef = activeFormDef(tenantId, rule.getFormId());
            forms.add(new FmProcessStartFormView(
                    rule.getFormId(),
                    rule.getFormVersionNo(),
                    formDef.getFormCode(),
                    formDef.getFormName(),
                    version.getSchemaContent(),
                    version.getUiSchemaContent(),
                    version.getCustomScriptContent(),
                    List.copyOf(taskKeysByForm.get(entry.getKey()))));
        }
        return List.copyOf(forms);
    }

    private FmFormDef activeFormDef(String tenantId, String formId)
            throws ServiceException {
        Map<String, Object> parameters = publishedParameters(tenantId);
        parameters.put("formId", formId);
        return formDefService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到流程綁定的表單主檔"));
    }

    private void validateTenantMembership(String tenantId, String account)
            throws ServiceException {
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("account", account);
        List<FmTenantAccount> memberships = tenantAccountService
                .selectListByParams(parameters).getValue();
        Date now = new Date();
        if (memberships == null || memberships.stream().noneMatch(value ->
                (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
                        && (value.getEffectiveTo() == null
                                || value.getEffectiveTo().after(now)))) {
            throw new ServiceException("目前帳號不屬於指定 Tenant");
        }
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

    private FmEmployeeOrgAssignment selectedOrPrimaryAssignment(
            String tenantId,
            FmEmployee applicant,
            Map<String, Object> formData) throws ServiceException {
        String formApplicantAccount = StringUtils.trimToNull(
                Objects.toString(formData.get(FmSystemFormFields.APPLICANT_ACCOUNT), null));
        if (formApplicantAccount != null
                && !applicant.getAccount().equals(formApplicantAccount)) {
            throw new ServiceException("表單申請人與送單申請人不一致");
        }
        String assignmentId = StringUtils.trimToNull(
                Objects.toString(
                        formData.get(FmSystemFormFields.APPLICANT_ASSIGNMENT_ID), null));
        if (assignmentId == null) {
            return primaryAssignment(tenantId, applicant.getEmployeeId());
        }
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("employeeId", applicant.getEmployeeId());
        parameters.put("employeeOrgAssignmentId", assignmentId);
        FmEmployeeOrgAssignment assignment = assignmentService
                .selectListByParams(parameters).getValue().stream()
                .filter(value -> isEffective(
                        value.getEffectiveFrom(), value.getEffectiveTo()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(
                        "申請部門不是申請人的有效任職"));
        String submittedOrgUnitId = StringUtils.trimToNull(
                Objects.toString(formData.get(FmSystemFormFields.APPLICANT_ORG_ID), null));
        if (submittedOrgUnitId != null
                && !assignment.getOrgUnitId().equals(submittedOrgUnitId)) {
            throw new ServiceException("申請部門與所選任職不一致");
        }
        return assignment;
    }

    private void authorizeStart(
            String tenantId,
            String processDefId,
            FmProcessVersion processVersion,
            FmEmployee applicant) throws ServiceException {
        Map<String, Object> assignmentParameters = activeParameters(tenantId);
        assignmentParameters.put("employeeId", applicant.getEmployeeId());
        Set<String> orgUnitIds = assignmentService
                .selectListByParams(assignmentParameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .map(FmEmployeeOrgAssignment::getOrgUnitId)
                .collect(Collectors.toSet());

        Map<String, Object> groupParameters = activeParameters(tenantId);
        groupParameters.put("employeeId", applicant.getEmployeeId());
        Set<String> groupIds = approvalGroupMemberService
                .selectListByParams(groupParameters).getValue().stream()
                .filter(value -> isEffective(value.getEffectiveFrom(), value.getEffectiveTo()))
                .map(FmApprovalGroupMember::getApprovalGroupId)
                .collect(Collectors.toSet());

        boolean allowed = startPolicyEvaluator.isAllowed(
                startPolicyService.findByVersion(
                        tenantId,
                        processDefId,
                        processVersion.getVersionNo()),
                new StartSubject(applicant.getAccount(), orgUnitIds, groupIds));
        if (!allowed) {
            throw new ServiceException("申請人沒有此流程的起單權限");
        }
    }

    private void authorizeProxy(
            String tenantId,
            String processDefId,
            String applicantAccount,
            String starterAccount)
            throws ServiceException {
        if (starterAccount.equals(applicantAccount)) {
            return;
        }
        Map<String, Object> parameters = activeParameters(tenantId);
        parameters.put("principalAccount", applicantAccount);
        parameters.put("proxyAccount", starterAccount);
        boolean authorized = startProxyEvaluator.isAuthorized(
                starterAccount,
                applicantAccount,
                processDefId,
                startProxyService.selectListByParams(parameters).getValue(),
                new Date());
        if (!authorized) {
            throw new ServiceException("目前登入者沒有替此申請人代起單的授權");
        }
    }

    private FmProcessSubmitView existingSubmission(
            FmProcessSubmitCommand command,
            String starterAccount) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", command.tenantId());
        parameters.put("idempotencyKey", command.idempotencyKey());
        List<FmFormData> formDataValues = formDataService
                .selectListByParams(parameters).getValue();
        if (formDataValues.isEmpty()) {
            return null;
        }
        if (formDataValues.size() != 1) {
            throw new ServiceException("Idempotency-Key 對應到多筆表單資料");
        }
        FmFormData formData = formDataValues.getFirst();
        parameters.clear();
        parameters.put("tenantId", command.tenantId());
        parameters.put("businessKey", formData.getBusinessKey());
        List<FmProcessInstance> processValues = processInstanceService
                .selectListByParams(parameters).getValue();
        if (processValues.size() != 1) {
            throw new ServiceException("Idempotency key 已被不完整的資料佔用");
        }
        FmProcessInstance process = processValues.getFirst();
        boolean sameRequest = command.formId().equals(formData.getFormId())
                && command.formVersionNo().equals(formData.getFormVersionNo())
                && command.applicantAccount().equals(formData.getOwnerAccount())
                && starterAccount.equals(process.getInitiatorAccount())
                && formData.getFormDataId().equals(process.getFormDataId())
                && sameJson(formData.getDataContent(), command.formData());
        if (!sameRequest) {
            throw new ServiceException(
                    "Idempotency key 已由另一筆不同的送單請求使用");
        }
        return new FmProcessSubmitView(
                formData.getBusinessKey(),
                formData.getDocumentNumber(),
                formData.getFormDataId(),
                process.getProcessInstanceId(),
                process.getInstanceStatus());
    }

    private boolean sameJson(String storedContent, Map<String, Object> submittedData) {
        try {
            return objectMapper.readTree(storedContent)
                    .equals(objectMapper.valueToTree(submittedData));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private FmFormData insertFormData(
            FmProcessSubmitCommand command,
            FmEmployeeOrgAssignment assignment,
            String businessKey,
            String documentNumber,
            Date now) {
        FmFormData value = new FmFormData();
        value.setTenantId(command.tenantId());
        value.setFormDataId(UUID.randomUUID().toString());
        value.setFormId(command.formId());
        value.setFormVersionNo(command.formVersionNo());
        value.setBusinessKey(businessKey);
        value.setDocumentNumber(documentNumber);
        value.setIdempotencyKey(command.idempotencyKey());
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
            String documentNumber,
            String starterAccount,
            Date now) {
        FmProcessInstance value = new FmProcessInstance();
        value.setTenantId(command.tenantId());
        value.setProcessInstanceId(flowableInstance.getId());
        value.setProcessDefId(command.processDefId());
        value.setProcessVersionNo(processVersion.getVersionNo());
        value.setFlowableProcessDefId(processVersion.getFlowableProcessDefId());
        value.setBusinessKey(businessKey);
        value.setDocumentNumber(documentNumber);
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

    private Map<String, Object> publishedParameters(String tenantId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "PUBLISHED");
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
