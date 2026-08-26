package org.qifu.fm.logic.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmProcessDefCommand;
import org.qifu.fm.dto.command.FmProcessCategoryCommand;
import org.qifu.fm.dto.command.FmProcessVersionCommand;
import org.qifu.fm.dto.command.FmProcessStartPolicyCommand;
import org.qifu.fm.dto.command.FmResolverPreviewCommand;
import org.qifu.fm.dto.command.FmTaskAssignmentRuleCommand;
import org.qifu.fm.dto.command.FmTaskFormRuleCommand;
import org.qifu.fm.dto.command.FmTaskPolicyCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmProcessCategoryView;
import org.qifu.fm.dto.view.FmProcessDefView;
import org.qifu.fm.dto.view.FmProcessVersionView;
import org.qifu.fm.dto.view.FmProcessStartPolicyView;
import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.dto.view.FmTaskFormRuleView;
import org.qifu.fm.dto.view.FmTaskPolicyView;
import org.qifu.fm.dto.view.FmTaskAssignmentRuleView;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessCategory;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmProcessStartPolicy;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.domain.resolver.IFmAssignmentResolverService;
import org.qifu.fm.domain.form.FmFormFieldCatalog;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.domain.runtime.FmApprovalGroupModeValidator;
import org.qifu.fm.domain.runtime.FmTaskFieldPolicyValidator;
import org.qifu.fm.domain.workflow.FmAssignmentRuleConfigValidator;
import org.qifu.fm.domain.workflow.FmBpmnDesignValidator;
import org.qifu.fm.domain.workflow.FmProcessPublishValidator;
import org.qifu.fm.logic.IFmProcessDefLogicService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessCategoryService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmProcessStartPolicyService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.qifu.fm.service.IFmOrgTitleService;
import org.qifu.fm.service.IFmOrgDutyService;
import org.qifu.fm.service.IFmOrgUnitService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessDefLogicServiceImpl implements IFmProcessDefLogicService {

    private final FmApprovalGroupModeValidator approvalGroupModeValidator =
            new FmApprovalGroupModeValidator(new tools.jackson.databind.ObjectMapper());
    private final FmAssignmentRuleConfigValidator assignmentRuleConfigValidator =
            new FmAssignmentRuleConfigValidator(new tools.jackson.databind.ObjectMapper());
    private final FmBpmnDesignValidator bpmnDesignValidator =
            new FmBpmnDesignValidator();
    private final FmProcessPublishValidator processPublishValidator =
            new FmProcessPublishValidator();
    private final FmFormFieldCatalog formFieldCatalog =
            new FmFormFieldCatalog(new tools.jackson.databind.ObjectMapper());
    private final FmTaskFieldPolicyValidator taskFieldPolicyValidator =
            new FmTaskFieldPolicyValidator(new tools.jackson.databind.ObjectMapper());

    private final IFmProcessDefService processDefService;
    private final IFmProcessCategoryService processCategoryService;
    private final IFmProcessVersionService processVersionService;
    private final IFmTenantService tenantService;
    private final IFmTaskFormRuleService taskFormRuleService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmTaskAssignmentRuleService assignmentRuleService;
    private final IFmProcessStartPolicyService startPolicyService;
    private final IFmAssignmentResolverService assignmentResolverService;
    private final IFmEmployeeService employeeService;
    private final IFmApprovalGroupService approvalGroupService;
    private final IFmOrgApprovalLevelService orgApprovalLevelService;
    private final IFmOrgTitleService orgTitleService;
    private final IFmOrgDutyService orgDutyService;
    private final IFmOrgUnitService orgUnitService;
    private final RepositoryService repositoryService;
    private final FmTenantAccessGuard tenantAccessGuard;

    public FmProcessDefLogicServiceImpl(
            IFmProcessDefService processDefService,
            IFmProcessCategoryService processCategoryService,
            IFmProcessVersionService processVersionService,
            IFmTenantService tenantService,
            IFmTaskFormRuleService taskFormRuleService,
            IFmTaskPolicyService taskPolicyService,
            IFmTaskAssignmentRuleService assignmentRuleService,
            IFmProcessStartPolicyService startPolicyService,
            IFmAssignmentResolverService assignmentResolverService,
            IFmEmployeeService employeeService,
            IFmApprovalGroupService approvalGroupService,
            IFmOrgApprovalLevelService orgApprovalLevelService,
            IFmOrgTitleService orgTitleService,
            IFmOrgDutyService orgDutyService,
            IFmOrgUnitService orgUnitService,
            RepositoryService repositoryService,
            FmTenantAccessGuard tenantAccessGuard) {
        this.processDefService = processDefService;
        this.processCategoryService = processCategoryService;
        this.processVersionService = processVersionService;
        this.tenantService = tenantService;
        this.taskFormRuleService = taskFormRuleService;
        this.taskPolicyService = taskPolicyService;
        this.assignmentRuleService = assignmentRuleService;
        this.startPolicyService = startPolicyService;
        this.assignmentResolverService = assignmentResolverService;
        this.employeeService = employeeService;
        this.approvalGroupService = approvalGroupService;
        this.orgApprovalLevelService = orgApprovalLevelService;
        this.orgTitleService = orgTitleService;
        this.orgDutyService = orgDutyService;
        this.orgUnitService = orgUnitService;
        this.repositoryService = repositoryService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> create(FmProcessDefCommand command)
            throws ServiceException {
        validate(command);
        tenantAccessGuard.requireAccess(command.tenantId());
        requireActiveCategory(command.tenantId(), command.category());
        assertUnique(command.tenantId(), command.processKey(), null);
        FmProcessDef processDef = new FmProcessDef();
        processDef.setTenantId(command.tenantId());
        processDef.setProcessDefId(UUID.randomUUID().toString());
        processDef.setProcessKey(command.processKey());
        processDef.setProcessName(command.processName());
        processDef.setCategory(command.category());
        processDef.setProcessSortOrder(normalizeSortOrder(command.processSortOrder()));
        processDef.setCurrentVersionNo(1);
        processDef.setStatus("DRAFT");
        processDef.setDescription(command.description());
        processDefService.insert(processDef);
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId(processDef.getTenantId());
        version.setProcessDefId(processDef.getProcessDefId());
        version.setVersionNo(1);
        version.setVersionStatus("DRAFT");
        version.setBpmnXml(defaultBpmn(processDef));
        version.setBpmnSha256(sha256(version.getBpmnXml()));
        processVersionService.insert(version);
        return load(processDef.getOid(), BaseSystemMessage.insertSuccess());
    }

    @Override
    public DefaultResult<FmProcessDefView> load(String oid, String message) throws ServiceException {
        FmProcessDef processDef = processDefService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(processDef.getTenantId());
        DefaultResult<FmProcessDefView> result = success(view(processDef));
        result.setMessage(message);
        return result;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> update(FmProcessDefCommand command)
            throws ServiceException {
        FmProcessDef processDef = processDefService.selectByPrimaryKey(command.oid())
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(processDef.getTenantId());
        if (!processDef.getTenantId().equals(command.tenantId())) {
            throw new ServiceException("不可變更流程所屬 Tenant");
        }
        if (StringUtils.isBlank(command.processName())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        if (StringUtils.isBlank(command.category())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        requireActiveCategory(command.tenantId(), command.category());
        processDef.setProcessName(command.processName());
        processDef.setCategory(command.category());
        processDef.setProcessSortOrder(normalizeSortOrder(command.processSortOrder()));
        processDef.setDescription(command.description());
        processDefService.update(processDef);
        return load(processDef.getOid(), BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> deactivate(String oid) throws ServiceException {
        FmProcessDef processDef = processDefService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(processDef.getTenantId());
        processDef.setStatus("INACTIVE");
        processDefService.update(processDef);
        return load(oid, BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> saveDraft(FmProcessVersionCommand command)
            throws ServiceException {
        FmProcessVersion version = draft(command.oid());
        if (StringUtils.isBlank(command.bpmnXml())) {
            throw new ServiceException("BPMN XML 不可空白");
        }
        validateBpmn(command.bpmnXml(), findDef(version.getTenantId(),
                version.getProcessDefId()).getProcessKey());
        Set<String> taskKeys = userTaskKeys(command.bpmnXml());
        saveTaskForms(version, command.taskForms(), taskKeys);
        saveTaskPolicies(version, command.taskPolicies(), taskKeys);
        saveAssignmentRules(version, command.assignmentRules(), taskKeys);
        saveStartPolicies(version, command.startPolicies());
        version.setBpmnXml(command.bpmnXml());
        version.setBpmnSha256(sha256(command.bpmnXml()));
        processVersionService.update(version);
        return loadByBusinessId(version.getTenantId(), version.getProcessDefId(),
                BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> createVersion(String processDefOid)
            throws ServiceException {
        FmProcessDef processDef = processDefService.selectByPrimaryKey(processDefOid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(processDef.getTenantId());
        List<FmProcessVersion> versions = versions(processDef);
        FmProcessVersion existingDraft = versions.stream()
                .filter(value -> "DRAFT".equals(value.getVersionStatus())).findFirst().orElse(null);
        if (existingDraft != null) {
            throw new ServiceException("已有草稿版本，請先編輯或發布該版本");
        }
        FmProcessVersion source = versions.get(0);
        int nextVersion = source.getVersionNo() + 1;
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId(processDef.getTenantId());
        version.setProcessDefId(processDef.getProcessDefId());
        version.setVersionNo(nextVersion);
        version.setVersionStatus("DRAFT");
        version.setBpmnXml(source.getBpmnXml());
        version.setBpmnSha256(sha256(source.getBpmnXml()));
        processVersionService.insert(version);
        List<FmTaskFormRule> copiedRules = taskFormRules(source).stream().map(sourceRule -> {
            FmTaskFormRule copy = newTaskFormRule(version, sourceRule.getTaskDefKey(),
                    sourceRule.getFormId(), sourceRule.getFormVersionNo());
            copy.setFieldPolicy(sourceRule.getFieldPolicy());
            return copy;
        }).toList();
        taskFormRuleService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), copiedRules);
        List<FmTaskPolicy> copiedPolicies = taskPolicies(source).stream()
                .map(sourcePolicy -> copyTaskPolicy(version, sourcePolicy))
                .toList();
        taskPolicyService.replaceVersion(
                version.getTenantId(),
                version.getProcessDefId(),
                version.getVersionNo(),
                copiedPolicies);
        List<FmTaskAssignmentRule> copiedAssignmentRules = assignmentRules(source).stream()
                .map(sourceRule -> copyAssignmentRule(version, sourceRule)).toList();
        assignmentRuleService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), copiedAssignmentRules);
        List<FmProcessStartPolicy> copiedStartPolicies = startPolicies(source).stream()
                .map(policy -> newStartPolicy(version, policy.getPolicySeq(),
                        policy.getSubjectType(), policy.getSubjectRefId(), policy.getAllowStart()))
                .toList();
        startPolicyService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), copiedStartPolicies);
        processDef.setCurrentVersionNo(nextVersion);
        processDef.setStatus("DRAFT");
        processDefService.update(processDef);
        return load(processDefOid, BaseSystemMessage.insertSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> publish(String versionOid) throws ServiceException {
        FmProcessVersion version = draft(versionOid);
        FmProcessDef processDef = findDef(version.getTenantId(), version.getProcessDefId());
        validateBpmn(version.getBpmnXml(), processDef.getProcessKey());
        Set<String> taskKeys = userTaskKeys(version.getBpmnXml());
        processPublishValidator.validate(taskKeys, taskFormRules(version),
                taskPolicies(version), assignmentRules(version));
        validateTaskFormsForPublish(version, taskKeys);
        validateGatewayFormFieldsForPublish(version);
        validateTaskPoliciesForPublish(version, taskKeys);
        validateAssignmentRulesForPublish(version);
        validateLevelMatchModesForPublish(version);
        validateApprovalGroupModesForPublish(version);
        validateStartPoliciesForPublish(version);
        String runtimeBpmnXml = runtimeBpmnXml(version);
        String resourceName = processDef.getProcessKey() + "-v" + version.getVersionNo()
                + ".bpmn20.xml";
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .tenantId(version.getTenantId())
                    .name(processDef.getProcessName() + " v" + version.getVersionNo())
                    .addString(resourceName, runtimeBpmnXml).deploy();
            ProcessDefinition deployed = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId()).singleResult();
            for (FmProcessVersion previous : versions(processDef)) {
                if ("PUBLISHED".equals(previous.getVersionStatus())) {
                    previous.setVersionStatus("RETIRED");
                    processVersionService.update(previous);
                }
            }
            version.setVersionStatus("PUBLISHED");
            version.setBpmnSha256(sha256(version.getBpmnXml()));
            version.setFlowableDeploymentId(deployment.getId());
            version.setFlowableProcessDefId(deployed.getId());
            version.setPublishedBy(UserUtils.getCurrentUser().getUserId());
            version.setPublishedDate(new Date());
            processVersionService.update(version);
            processDef.setCurrentVersionNo(version.getVersionNo());
            processDef.setStatus("PUBLISHED");
            processDefService.update(processDef);
            return load(processDef.getOid(), "流程版本發布成功");
        } catch (RuntimeException exception) {
            throw new ServiceException("BPMN 發布失敗：" + exception.getMessage());
        }
    }

    @Override
    public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "ACTIVE");
        var tenantIds = UserUtils.isAdmin() ? null : tenantAccessGuard.accessibleTenantIds();
        return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue()
                .stream().filter(value -> tenantIds == null
                        || tenantIds.contains(value.getTenantId()))
                .map(value -> new FmOptionView(value.getTenantId(),
                        value.getTenantCode() + "：" + value.getTenantName())).toList());
    }

    @Override
    public DefaultResult<List<FmPublishedFormOptionView>> publishedFormOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        return success(taskFormRuleService.publishedFormOptions(tenantId));
    }

    private String runtimeBpmnXml(FmProcessVersion version) throws ServiceException {
        try {
            String sourceXml = version.getBpmnXml();
            Map<String, FmTaskPolicy> policies = taskPolicies(version).stream()
                    .collect(Collectors.toMap(FmTaskPolicy::getTaskDefKey,
                            policy -> policy));
            javax.xml.stream.XMLStreamReader reader = javax.xml.stream.XMLInputFactory
                    .newFactory().createXMLStreamReader(new java.io.StringReader(sourceXml));
            org.flowable.bpmn.converter.BpmnXMLConverter converter =
                    new org.flowable.bpmn.converter.BpmnXMLConverter();
            org.flowable.bpmn.model.BpmnModel model = converter.convertToBpmnModel(reader);
            for (org.flowable.bpmn.model.UserTask userTask
                    : model.getMainProcess().findFlowElementsOfType(
                            org.flowable.bpmn.model.UserTask.class)) {
                boolean exists = userTask.getTaskListeners().stream()
                        .anyMatch(listener -> "create".equals(listener.getEvent())
                                && "${fmTaskAssignmentListener}"
                                        .equals(listener.getImplementation()));
                if (exists) {
                    continue;
                }
                org.flowable.bpmn.model.FlowableListener listener =
                        new org.flowable.bpmn.model.FlowableListener();
                listener.setEvent("create");
                listener.setImplementationType(
                        org.flowable.bpmn.model.ImplementationType
                                .IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
                listener.setImplementation("${fmTaskAssignmentListener}");
                userTask.getTaskListeners().add(listener);
                FmTaskPolicy policy = policies.get(userTask.getId());
                if (policy != null && Set.of("ALL", "SEQUENTIAL")
                        .contains(policy.getAssignmentMode())) {
                    org.flowable.bpmn.model.MultiInstanceLoopCharacteristics loop =
                            new org.flowable.bpmn.model.MultiInstanceLoopCharacteristics();
                    loop.setInputDataItem("${fmTaskAssignmentListener.multiInstanceAccounts(execution, '"
                            + userTask.getId() + "')}");
                    loop.setElementVariable("flowmintAssignee");
                    loop.setSequential("SEQUENTIAL".equals(policy.getAssignmentMode()));
                    userTask.setLoopCharacteristics(loop);
                }
            }
            return new String(converter.convertToXML(model), StandardCharsets.UTF_8);
        } catch (RuntimeException | javax.xml.stream.XMLStreamException exception) {
            throw new ServiceException("無法建立 FlowMint Runtime BPMN：" + exception.getMessage());
        }
    }

    @Override
    public DefaultResult<List<FmResolverPreviewView>> resolverPreview(
            FmResolverPreviewCommand command) throws ServiceException {
        if (command == null || StringUtils.isAnyBlank(
                command.versionOid(), command.initiatorAccount())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        FmProcessVersion version = processVersionService.selectByPrimaryKey(command.versionOid())
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(version.getTenantId());
        List<FmResolverPreviewView> previews = assignmentRules(version).stream()
                .filter(rule -> "ACTIVE".equals(rule.getStatus()))
                .map(rule -> resolvePreview(
                        rule,
                        command.initiatorAccount(),
                        command.variables()))
                .toList();
        return success(previews);
    }

    private FmResolverPreviewView resolvePreview(
            FmTaskAssignmentRule rule,
            String initiatorAccount,
            Map<String, Object> variables) {
        try {
            return assignmentResolverService.resolve(rule, initiatorAccount, variables);
        } catch (ServiceException exception) {
            return new FmResolverPreviewView(rule.getTaskDefKey(), rule.getRuleSeq(),
                    rule.getResolverType(), "ERROR", exception.getMessage(), List.of());
        }
    }

    @Override
    public DefaultResult<List<FmOptionView>> resolverAccountOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return success(employeeService.selectListByParams(parameters, "EMPLOYEE_NO", "ASC")
                .getValue().stream().map(employee -> new FmOptionView(employee.getAccount(),
                        employee.getEmployeeNo() + "／" + employee.getDisplayName())).toList());
    }

    @Override
    public DefaultResult<List<FmOptionView>> approvalGroupOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return success(approvalGroupService.selectListByParams(parameters, "GROUP_CODE", "ASC")
                .getValue().stream().map(group -> new FmOptionView(group.getApprovalGroupId(),
                        group.getGroupCode() + "／" + group.getGroupName())).toList());
    }

    @Override
    public DefaultResult<List<FmOptionView>> approvalLevelOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return success(orgApprovalLevelService
                .selectListByParams(parameters, "LEVEL_ORDER", "ASC").getValue().stream()
                .map(level -> new FmOptionView(level.getApprovalLevelId(),
                        level.getLevelCode() + "／" + level.getLevelName())).toList());
    }

    @Override
    public DefaultResult<List<FmOptionView>> orgTitleOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = activeOptionParameters(tenantId);
        return success(orgTitleService.selectListByParams(parameters, "SORT_NO", "ASC")
                .getValue().stream().map(title -> new FmOptionView(title.getTitleId(),
                        title.getTitleCode() + "／" + title.getTitleName())).toList());
    }

    @Override
    public DefaultResult<List<FmOptionView>> orgDutyOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = activeOptionParameters(tenantId);
        return success(orgDutyService.selectListByParams(parameters, "DUTY_CODE", "ASC")
                .getValue().stream().map(duty -> new FmOptionView(duty.getDutyId(),
                        duty.getDutyCode() + "／" + duty.getDutyName())).toList());
    }

    @Override
    public DefaultResult<List<FmOptionView>> orgUnitOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        return success(orgUnitService.selectListByParams(parameters, "UNIT_CODE", "ASC")
                .getValue().stream().map(unit -> new FmOptionView(
                        unit.getOrgUnitId(), unit.getUnitCode())).toList());
    }

    private Map<String, Object> activeOptionParameters(String tenantId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        return parameters;
    }

    @Override
    public DefaultResult<List<FmProcessCategoryView>> categoryOptions(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        List<FmProcessCategoryView> values = processCategoryService
                .selectListByParams(activeOptionParameters(tenantId),
                        "SORT_ORDER,CATEGORY_LABEL", "ASC")
                .getValue().stream()
                .map(value -> new FmProcessCategoryView(
                        value.getOid(), value.getTenantId(), value.getCategoryCode(),
                        value.getCategoryLabel(), value.getIconCode(),
                        value.getSortOrder(), value.getStatus()))
                .toList();
        return success(values);
    }

    @Override
    public DefaultResult<List<FmProcessCategoryView>> categoryList(String tenantId)
            throws ServiceException {
        validateTenantId(tenantId);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        List<FmProcessCategoryView> values = processCategoryService
                .selectListByParams(parameters, "SORT_ORDER,CATEGORY_LABEL", "ASC")
                .getValue().stream().map(this::categoryView).toList();
        return success(values);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<List<FmProcessCategoryView>> saveCategory(
            FmProcessCategoryCommand command) throws ServiceException {
        if (command == null || StringUtils.isAnyBlank(
                command.tenantId(), command.categoryCode(), command.categoryLabel())
                || !command.categoryCode().matches("[A-Za-z][A-Za-z0-9_-]*")) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        validateTenantId(command.tenantId());
        FmProcessCategory category;
        if (StringUtils.isBlank(command.oid())) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("tenantId", command.tenantId());
            parameters.put("categoryCode", command.categoryCode());
            if (!processCategoryService.selectListByParams(parameters).getValue().isEmpty()) {
                throw new ServiceException("同一 Tenant 的分類代碼不可重複");
            }
            category = new FmProcessCategory();
            category.setTenantId(command.tenantId());
            category.setCategoryCode(command.categoryCode());
            category.setStatus("ACTIVE");
            applyCategory(command, category);
            processCategoryService.insert(category);
        } else {
            category = processCategoryService.selectByPrimaryKey(command.oid())
                    .getValueEmptyThrowMessage();
            tenantAccessGuard.requireAccess(category.getTenantId());
            if (!category.getTenantId().equals(command.tenantId())
                    || !category.getCategoryCode().equals(command.categoryCode())) {
                throw new ServiceException("不可變更分類的 Tenant 或代碼");
            }
            applyCategory(command, category);
            category.setStatus("INACTIVE".equals(command.status())
                    ? "INACTIVE" : "ACTIVE");
            processCategoryService.update(category);
        }
        return categoryList(command.tenantId());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<List<FmProcessCategoryView>> deactivateCategory(String oid)
            throws ServiceException {
        FmProcessCategory category = processCategoryService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(category.getTenantId());
        Map<String, Object> references = new HashMap<>();
        references.put("tenantId", category.getTenantId());
        references.put("category", category.getCategoryCode());
        references.put("notStatus", "INACTIVE");
        if (!processDefService.selectListByParams(references).getValue().isEmpty()) {
            throw new ServiceException("分類仍有未停用流程使用，不可停用");
        }
        category.setStatus("INACTIVE");
        processCategoryService.update(category);
        return categoryList(category.getTenantId());
    }

    private void applyCategory(FmProcessCategoryCommand command,
            FmProcessCategory category) {
        category.setCategoryLabel(command.categoryLabel().trim());
        category.setIconCode(StringUtils.trimToNull(command.iconCode()));
        category.setSortOrder(normalizeSortOrder(command.sortOrder()));
    }

    private FmProcessCategoryView categoryView(FmProcessCategory value) {
        return new FmProcessCategoryView(value.getOid(), value.getTenantId(),
                value.getCategoryCode(), value.getCategoryLabel(),
                value.getIconCode(), value.getSortOrder(), value.getStatus());
    }

    private void validateTenantId(String tenantId) throws ServiceException {
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        tenantAccessGuard.requireAccess(tenantId);
    }

    @Override
    public FmProcessDefView view(FmProcessDef processDef) throws ServiceException {
        List<FmProcessVersionView> versionViews = versions(processDef).stream()
                .map(value -> new FmProcessVersionView(value.getOid(), value.getVersionNo(),
                        value.getVersionStatus(), value.getBpmnXml(), value.getBpmnSha256(),
                        value.getFlowableDeploymentId(), value.getFlowableProcessDefId(),
                        value.getPublishedBy(), value.getPublishedDate(),
                        taskFormRules(value).stream().map(rule -> new FmTaskFormRuleView(
                                rule.getTaskDefKey(), rule.getFormId(),
                                rule.getFormVersionNo(), rule.getFieldPolicy())).toList(),
                        taskPolicies(value).stream().map(policy -> new FmTaskPolicyView(
                                policy.getTaskDefKey(),
                                policy.getTaskName(),
                                policy.getAssignmentMode(),
                                policy.getSelfApprovalPolicy(),
                                policy.getDuplicatePolicy(),
                                policy.getAllowReject(),
                                policy.getAllowReturn(),
                                policy.getAllowTransfer(),
                                policy.getAllowAddSign(),
                                policy.getAllowParallelAddSign(),
                                policy.getParallelAddSignMaxMembers(),
                                policy.getParallelAddSignCommentRequired(),
                                policy.getCommentRequired(),
                                policy.getDueHours(),
                                policy.getReminderBeforeHours())).toList(),
                        assignmentRules(value).stream().map(rule -> new FmTaskAssignmentRuleView(
                                rule.getTaskDefKey(), rule.getRuleSeq(), rule.getResolverType(),
                                rule.getResolverConfig(), rule.getFallbackConfig(),
                                rule.getMaxResults(), rule.getStatus())).toList(),
                        startPolicies(value).stream().map(policy -> new FmProcessStartPolicyView(
                                policy.getPolicySeq(), policy.getSubjectType(),
                                policy.getSubjectRefId(), policy.getAllowStart())).toList()))
                .toList();
        return new FmProcessDefView(processDef.getOid(), processDef.getTenantId(),
                processDef.getProcessDefId(), processDef.getProcessKey(),
                processDef.getProcessName(), processDef.getCategory(),
                processDef.getProcessSortOrder(), processDef.getDocumentType(),
                processDef.getCurrentVersionNo(), processDef.getStatus(),
                processDef.getDescription(), versionViews);
    }

    private void validate(FmProcessDefCommand command) throws ServiceException {
        if (StringUtils.isAnyBlank(command.tenantId(), command.processKey(),
                command.processName(), command.category())
                || !command.processKey().matches("[A-Za-z][A-Za-z0-9_-]*")) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
    }

    private void requireActiveCategory(String tenantId, String categoryCode)
            throws ServiceException {
        Map<String, Object> parameters = activeOptionParameters(tenantId);
        parameters.put("categoryCode", categoryCode);
        if (processCategoryService.selectListByParams(parameters).getValue().isEmpty()) {
            throw new ServiceException("流程分類不存在或未啟用");
        }
    }

    private int normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private void assertUnique(String tenantId, String processKey, String excludedOid)
            throws ServiceException {
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("processKey", processKey);
        boolean exists = processDefService.selectListByParams(params).getValue().stream()
                .anyMatch(value -> !value.getOid().equals(excludedOid));
        if (exists) {
            throw new ServiceException("同一 Tenant 的流程代碼不可重複");
        }
    }

    private FmProcessVersion draft(String oid) throws ServiceException {
        FmProcessVersion version = processVersionService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(version.getTenantId());
        if (!"DRAFT".equals(version.getVersionStatus())) {
            throw new ServiceException("已發布或已退役版本不可修改");
        }
        return version;
    }

    private List<FmProcessVersion> versions(FmProcessDef processDef) throws ServiceException {
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", processDef.getTenantId());
        params.put("processDefId", processDef.getProcessDefId());
        return processVersionService.selectListByParams(params, "VERSION_NO", "DESC").getValue();
    }

    private FmProcessDef findDef(String tenantId, String processDefId) throws ServiceException {
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("processDefId", processDefId);
        return processDefService.selectListByParams(params).getValue().stream().findFirst()
                .orElseThrow(() -> new ServiceException(BaseSystemMessage.dataNoExist()));
    }

    private DefaultResult<FmProcessDefView> loadByBusinessId(
            String tenantId, String processDefId, String message) throws ServiceException {
        return load(findDef(tenantId, processDefId).getOid(), message);
    }

    private void validateBpmn(String xml, String expectedProcessKey) throws ServiceException {
        bpmnDesignValidator.validate(xml, expectedProcessKey);
    }

    private Set<String> userTaskKeys(String xml) throws ServiceException {
        try {
            javax.xml.stream.XMLStreamReader reader = javax.xml.stream.XMLInputFactory
                    .newFactory().createXMLStreamReader(new java.io.StringReader(xml));
            org.flowable.bpmn.model.BpmnModel model =
                    new org.flowable.bpmn.converter.BpmnXMLConverter().convertToBpmnModel(reader);
            return model.getMainProcess().findFlowElementsOfType(
                    org.flowable.bpmn.model.UserTask.class).stream()
                    .map(org.flowable.bpmn.model.UserTask::getId).collect(Collectors.toSet());
        } catch (Exception exception) {
            throw new ServiceException("無法讀取 BPMN User Task：" + exception.getMessage());
        }
    }

    private List<FmTaskFormRule> taskFormRules(FmProcessVersion version)
            throws ServiceException {
        return taskFormRuleService.findByVersion(version.getTenantId(),
                version.getProcessDefId(), version.getVersionNo());
    }

    private List<FmTaskPolicy> taskPolicies(FmProcessVersion version)
            throws ServiceException {
        return taskPolicyService.findByVersion(
                version.getTenantId(),
                version.getProcessDefId(),
                version.getVersionNo());
    }

    private List<FmTaskAssignmentRule> assignmentRules(FmProcessVersion version)
            throws ServiceException {
        return assignmentRuleService.findByVersion(version.getTenantId(),
                version.getProcessDefId(), version.getVersionNo());
    }

    private List<FmProcessStartPolicy> startPolicies(FmProcessVersion version)
            throws ServiceException {
        return startPolicyService.findByVersion(version.getTenantId(),
                version.getProcessDefId(), version.getVersionNo());
    }

    private void saveStartPolicies(FmProcessVersion version,
            List<FmProcessStartPolicyCommand> commands) throws ServiceException {
        if (commands == null) {
            return;
        }
        Set<Integer> sequences = new java.util.HashSet<>();
        Set<String> accounts = employeeService.selectListByParams(
                activeOptionParameters(version.getTenantId())).getValue().stream()
                .map(employee -> employee.getAccount()).collect(Collectors.toSet());
        Set<String> groupIds = approvalGroupService.selectListByParams(
                activeOptionParameters(version.getTenantId())).getValue().stream()
                .map(group -> group.getApprovalGroupId()).collect(Collectors.toSet());
        Map<String, Object> unitParameters = new HashMap<>();
        unitParameters.put("tenantId", version.getTenantId());
        Set<String> orgUnitIds = orgUnitService.selectListByParams(unitParameters).getValue()
                .stream().map(unit -> unit.getOrgUnitId()).collect(Collectors.toSet());
        List<FmProcessStartPolicy> policies = new java.util.ArrayList<>();
        for (FmProcessStartPolicyCommand command : commands) {
            if (command == null || command.policySeq() == null || command.policySeq() < 1
                    || !sequences.add(command.policySeq())
                    || !Set.of("ALL", "ACCOUNT", "ORG_UNIT", "APPROVAL_GROUP")
                            .contains(command.subjectType())
                    || !Set.of("Y", "N").contains(command.allowStart())
                    || (!"ALL".equals(command.subjectType())
                            && StringUtils.isBlank(command.subjectRefId()))) {
                throw new ServiceException("流程啟動規則內容不完整或重複");
            }
            boolean missingReference = switch (command.subjectType()) {
                case "ACCOUNT" -> !accounts.contains(command.subjectRefId());
                case "APPROVAL_GROUP" -> !groupIds.contains(command.subjectRefId());
                case "ORG_UNIT" -> !orgUnitIds.contains(command.subjectRefId());
                default -> false;
            };
            if (missingReference) {
                throw new ServiceException("流程啟動規則的對象不存在或不屬於目前 Tenant："
                        + command.subjectRefId());
            }
            policies.add(newStartPolicy(version, command.policySeq(), command.subjectType(),
                    "ALL".equals(command.subjectType()) ? null : command.subjectRefId(),
                    command.allowStart()));
        }
        startPolicyService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), policies);
    }

    private FmProcessStartPolicy newStartPolicy(FmProcessVersion version, Integer sequence,
            String subjectType, String subjectRefId, String allowStart) {
        FmProcessStartPolicy policy = new FmProcessStartPolicy();
        policy.setOid(UUID.randomUUID().toString());
        policy.setTenantId(version.getTenantId());
        policy.setProcessDefId(version.getProcessDefId());
        policy.setProcessVersionNo(version.getVersionNo());
        policy.setPolicySeq(sequence);
        policy.setSubjectType(subjectType);
        policy.setSubjectRefId(subjectRefId);
        policy.setAllowStart(allowStart);
        policy.setCuserid(UserUtils.getCurrentUser().getUserId());
        policy.setCdate(new Date());
        return policy;
    }

    private void validateStartPoliciesForPublish(FmProcessVersion version)
            throws ServiceException {
        List<FmProcessStartPolicy> policies = startPolicies(version);
        if (policies.isEmpty()
                || policies.stream().noneMatch(policy -> "Y".equals(policy.getAllowStart()))) {
            throw new ServiceException("流程至少需要一筆允許啟動的規則");
        }
    }

    private void saveAssignmentRules(FmProcessVersion version,
            List<FmTaskAssignmentRuleCommand> commands, Set<String> taskKeys)
            throws ServiceException {
        if (commands == null) {
            return;
        }
        Set<String> identities = new java.util.HashSet<>();
        List<FmTaskAssignmentRule> rules = new java.util.ArrayList<>();
        for (FmTaskAssignmentRuleCommand command : commands) {
            if (command == null || StringUtils.isAnyBlank(command.taskDefKey(), command.resolverType())
                    || !taskKeys.contains(command.taskDefKey()) || command.ruleSeq() == null
                    || command.ruleSeq() < 1
                    || !identities.add(command.taskDefKey() + ":" + command.ruleSeq())
                    || !resolverTypes().contains(command.resolverType())
                    || command.maxResults() == null || command.maxResults() < 1
                    || command.maxResults() > 1000
                    || !Set.of("ACTIVE", "INACTIVE").contains(command.status())) {
                throw new ServiceException("User Task 簽核人規則不完整或重複");
            }
            FmTaskAssignmentRule rule = new FmTaskAssignmentRule();
            rule.setOid(UUID.randomUUID().toString());
            rule.setTenantId(version.getTenantId());
            rule.setProcessDefId(version.getProcessDefId());
            rule.setProcessVersionNo(version.getVersionNo());
            rule.setTaskDefKey(command.taskDefKey());
            rule.setRuleSeq(command.ruleSeq());
            rule.setResolverType(command.resolverType());
            rule.setResolverConfig(StringUtils.defaultIfBlank(command.resolverConfig(), "{}"));
            rule.setFallbackConfig(command.fallbackConfig());
            assignmentRuleConfigValidator.validate(
                    rule.getResolverType(),
                    rule.getResolverConfig(),
                    rule.getFallbackConfig());
            rule.setMaxResults(command.maxResults());
            rule.setStatus(command.status());
            rule.setCuserid(UserUtils.getCurrentUser().getUserId());
            rule.setCdate(new Date());
            rules.add(rule);
        }
        assignmentRuleService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), rules);
    }

    private Set<String> resolverTypes() {
        return Set.of("FIXED_ACCOUNT", "APPROVAL_GROUP", "INITIATOR_ORG_HEAD",
                "PARENT_ORG_HEAD", "NEXT_HIGHER_LEVEL_HEAD", "TARGET_LEVEL_HEAD",
                "LEVEL_HEAD_CHAIN", "ROOT_ORG_HEAD", "DIRECT_MANAGER", "MANAGER_CHAIN",
                "ORG_TITLE", "ORG_DUTY", "APPROVAL_AUTHORITY", "FORM_ACCOUNT_FIELD");
    }

    private FmTaskAssignmentRule copyAssignmentRule(FmProcessVersion version,
            FmTaskAssignmentRule source) {
        FmTaskAssignmentRule result = new FmTaskAssignmentRule();
        result.setOid(UUID.randomUUID().toString());
        result.setTenantId(version.getTenantId());
        result.setProcessDefId(version.getProcessDefId());
        result.setProcessVersionNo(version.getVersionNo());
        result.setTaskDefKey(source.getTaskDefKey());
        result.setRuleSeq(source.getRuleSeq());
        result.setResolverType(source.getResolverType());
        result.setResolverConfig(source.getResolverConfig());
        result.setFallbackConfig(source.getFallbackConfig());
        result.setMaxResults(source.getMaxResults());
        result.setStatus(source.getStatus());
        result.setCuserid(UserUtils.getCurrentUser().getUserId());
        result.setCdate(new Date());
        return result;
    }

    private void saveTaskPolicies(
            FmProcessVersion version,
            List<FmTaskPolicyCommand> commands,
            Set<String> taskKeys) throws ServiceException {
        if (commands == null) {
            return;
        }
        Set<String> submittedKeys = new java.util.HashSet<>();
        List<FmTaskPolicy> policies = new java.util.ArrayList<>();
        for (FmTaskPolicyCommand command : commands) {
            validateTaskPolicyCommand(command, taskKeys, submittedKeys);
            policies.add(newTaskPolicy(version, command));
        }
        taskPolicyService.replaceVersion(
                version.getTenantId(),
                version.getProcessDefId(),
                version.getVersionNo(),
                policies);
    }

    private void validateTaskPolicyCommand(
            FmTaskPolicyCommand command,
            Set<String> taskKeys,
            Set<String> submittedKeys) throws ServiceException {
        if (command == null
                || StringUtils.isAnyBlank(command.taskDefKey(), command.taskName())
                || !taskKeys.contains(command.taskDefKey())
                || !submittedKeys.add(command.taskDefKey())
                || !Set.of("ASSIGNEE", "CANDIDATE", "ALL", "SEQUENTIAL",
                        "APPLICANT_CORRECTION")
                        .contains(command.assignmentMode())
                || !Set.of("ALLOW", "SKIP_TO_NEXT", "REQUIRE_ALTERNATE", "INCIDENT")
                        .contains(command.selfApprovalPolicy())
                || !Set.of("KEEP_EACH_LEVEL", "MERGE_CONSECUTIVE", "SKIP_ALREADY_APPROVED")
                        .contains(command.duplicatePolicy())
                || !Set.of("NEVER", "ALWAYS", "ON_REJECT_RETURN")
                        .contains(command.commentRequired())
                || !isYesNo(command.allowReject())
                || !isYesNo(command.allowReturn())
                || !isYesNo(command.allowTransfer())
                || !isYesNo(command.allowAddSign())
                || !isYesNo(command.allowParallelAddSign())
                || !validParallelAddSignPolicy(command)
                || !validSla(command.dueHours(), command.reminderBeforeHours())) {
            throw new ServiceException("User Task 政策設定不正確或同一節點重複設定");
        }
    }

    private boolean validParallelAddSignPolicy(FmTaskPolicyCommand command) {
        return command.parallelAddSignMaxMembers() != null
                && command.parallelAddSignMaxMembers() >= 1
                && command.parallelAddSignMaxMembers() <= 20
                && isYesNo(command.parallelAddSignCommentRequired());
    }

    private boolean isYesNo(String value) {
        return "Y".equals(value) || "N".equals(value);
    }

    private boolean validSla(Integer dueHours, Integer reminderBeforeHours) {
        if (dueHours == null) {
            return reminderBeforeHours == null;
        }
        return dueHours >= 1 && dueHours <= 8760
                && (reminderBeforeHours == null
                        || (reminderBeforeHours >= 0 && reminderBeforeHours < dueHours));
    }

    private FmTaskPolicy newTaskPolicy(
            FmProcessVersion version,
            FmTaskPolicyCommand command) {
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setOid(UUID.randomUUID().toString());
        policy.setTenantId(version.getTenantId());
        policy.setProcessDefId(version.getProcessDefId());
        policy.setProcessVersionNo(version.getVersionNo());
        policy.setTaskDefKey(command.taskDefKey());
        policy.setTaskName(command.taskName());
        policy.setAssignmentMode(command.assignmentMode());
        policy.setSelfApprovalPolicy(command.selfApprovalPolicy());
        policy.setDuplicatePolicy(command.duplicatePolicy());
        policy.setAllowReject(command.allowReject());
        policy.setAllowReturn(command.allowReturn());
        policy.setAllowTransfer(command.allowTransfer());
        policy.setAllowAddSign(command.allowAddSign());
        policy.setAllowParallelAddSign(command.allowParallelAddSign());
        policy.setParallelAddSignMaxMembers(command.parallelAddSignMaxMembers());
        policy.setParallelAddSignCommentRequired(
                command.parallelAddSignCommentRequired());
        policy.setCommentRequired(command.commentRequired());
        policy.setDueHours(command.dueHours());
        policy.setReminderBeforeHours(command.reminderBeforeHours());
        policy.setCuserid(UserUtils.getCurrentUser().getUserId());
        policy.setCdate(new Date());
        return policy;
    }

    private FmTaskPolicy copyTaskPolicy(
            FmProcessVersion version,
            FmTaskPolicy source) {
        FmTaskPolicyCommand command = new FmTaskPolicyCommand(
                source.getTaskDefKey(),
                source.getTaskName(),
                source.getAssignmentMode(),
                source.getSelfApprovalPolicy(),
                source.getDuplicatePolicy(),
                source.getAllowReject(),
                source.getAllowReturn(),
                source.getAllowTransfer(),
                source.getAllowAddSign(),
                source.getAllowParallelAddSign(),
                source.getParallelAddSignMaxMembers(),
                source.getParallelAddSignCommentRequired(),
                source.getCommentRequired(),
                source.getDueHours(),
                source.getReminderBeforeHours());
        return newTaskPolicy(version, command);
    }

    private void saveTaskForms(FmProcessVersion version,
            List<FmTaskFormRuleCommand> commands, Set<String> taskKeys) throws ServiceException {
        if (commands == null) {
            return;
        }
        Map<String, FmTaskFormRule> existing = taskFormRules(version).stream()
                .collect(Collectors.toMap(FmTaskFormRule::getTaskDefKey, value -> value));
        Set<String> submittedKeys = new java.util.HashSet<>();
        List<FmTaskFormRule> rules = new java.util.ArrayList<>();
        for (FmTaskFormRuleCommand command : commands) {
            if (command == null || StringUtils.isAnyBlank(command.taskDefKey(), command.formId())
                    || command.formVersionNo() == null || command.formVersionNo() < 1
                    || !taskKeys.contains(command.taskDefKey())
                    || !submittedKeys.add(command.taskDefKey())) {
                throw new ServiceException("User Task 表單設定不正確或同一節點重複設定");
            }
            if (!taskFormRuleService.isPublishedFormVersion(version.getTenantId(),
                    command.formId(), command.formVersionNo())) {
                throw new ServiceException("User Task「" + command.taskDefKey()
                        + "」選擇的表單版本不存在或尚未發布");
            }
            FmTaskFormRule rule = newTaskFormRule(version, command.taskDefKey(),
                    command.formId(), command.formVersionNo());
            if (StringUtils.isNotBlank(command.fieldPolicy())) {
                rule.setFieldPolicy(command.fieldPolicy().trim());
            }
            FmTaskFormRule previous = existing.get(command.taskDefKey());
            if (previous != null && command.formId().equals(previous.getFormId())
                    && command.formVersionNo().equals(previous.getFormVersionNo())
                    && StringUtils.isBlank(command.fieldPolicy())) {
                rule.setFieldPolicy(previous.getFieldPolicy());
            }
            taskFieldPolicyValidator.validateConfiguration(rule.getFieldPolicy());
            rules.add(rule);
        }
        taskFormRuleService.replaceVersion(version.getTenantId(), version.getProcessDefId(),
                version.getVersionNo(), rules);
    }

    private FmTaskFormRule newTaskFormRule(FmProcessVersion version, String taskDefKey,
            String formId, Integer formVersionNo) {
        FmTaskFormRule rule = new FmTaskFormRule();
        rule.setOid(UUID.randomUUID().toString());
        rule.setTenantId(version.getTenantId());
        rule.setProcessDefId(version.getProcessDefId());
        rule.setProcessVersionNo(version.getVersionNo());
        rule.setTaskDefKey(taskDefKey);
        rule.setFormId(formId);
        rule.setFormVersionNo(formVersionNo);
        rule.setFieldPolicy(String.format("{%cdefault%c:%cREAD%c,%cfields%c:{}}",
                34, 34, 34, 34, 34, 34));
        rule.setCuserid(UserUtils.getCurrentUser().getUserId());
        rule.setCdate(new Date());
        return rule;
    }

    private void validateTaskFormsForPublish(FmProcessVersion version, Set<String> taskKeys)
            throws ServiceException {
        Map<String, FmTaskFormRule> rules = taskFormRules(version).stream()
                .collect(Collectors.toMap(FmTaskFormRule::getTaskDefKey, value -> value));
        for (String taskKey : taskKeys) {
            FmTaskFormRule rule = rules.get(taskKey);
            if (rule == null) {
                throw new ServiceException("User Task「" + taskKey + "」尚未選擇顯示表單");
            }
            if (!taskFormRuleService.isPublishedFormVersion(version.getTenantId(),
                    rule.getFormId(), rule.getFormVersionNo())) {
                throw new ServiceException("User Task「" + taskKey + "」引用的表單版本已失效");
            }
        }
    }

    private void validateTaskPoliciesForPublish(
            FmProcessVersion version,
            Set<String> taskKeys) throws ServiceException {
        List<FmTaskPolicy> policies = taskPolicies(version);
        Set<String> policyKeys = policies.stream()
                .map(FmTaskPolicy::getTaskDefKey)
                .collect(Collectors.toSet());
        for (String taskKey : taskKeys) {
            if (!policyKeys.contains(taskKey)) {
                throw new ServiceException(
                        "User Task「" + taskKey + "」尚未設定 Task Policy");
            }
        }
        boolean hasCorrectionTask = policies.stream().anyMatch(policy ->
                "APPLICANT_CORRECTION".equals(policy.getAssignmentMode()));
        if (!hasCorrectionTask && policies.stream()
                .anyMatch(policy -> "Y".equals(policy.getAllowReturn()))) {
            throw new ServiceException("流程允許退回時，必須至少設定一個申請人補件節點");
        }
        Map<String, Long> activeRuleCounts = assignmentRules(version).stream()
                .filter(rule -> "ACTIVE".equals(rule.getStatus()))
                .collect(Collectors.groupingBy(FmTaskAssignmentRule::getTaskDefKey,
                        Collectors.counting()));
        for (String taskKey : taskKeys) {
            boolean correctionTask = policies.stream()
                    .anyMatch(policy -> taskKey.equals(policy.getTaskDefKey())
                            && "APPLICANT_CORRECTION".equals(policy.getAssignmentMode()));
            if (correctionTask) {
                continue;
            }
            if (activeRuleCounts.getOrDefault(taskKey, 0L) < 1) {
                throw new ServiceException(
                        "User Task " + taskKey + " 尚未設定啟用的簽核人規則");
            }
        }
    }

    private void validateApprovalGroupModesForPublish(FmProcessVersion version)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", version.getTenantId());
        parameters.put("status", "ACTIVE");
        Map<String, String> groupModes = approvalGroupService
                .selectListByParams(parameters).getValue().stream()
                .collect(Collectors.toMap(
                        FmApprovalGroup::getApprovalGroupId,
                        FmApprovalGroup::getAssignmentMode));
        approvalGroupModeValidator.validate(
                taskPolicies(version), assignmentRules(version), groupModes);
    }

    private void validateAssignmentRulesForPublish(FmProcessVersion version)
            throws ServiceException {
        Map<String, FmPublishedFormOptionView> forms = taskFormRuleService
                .publishedFormOptions(version.getTenantId()).stream()
                .collect(Collectors.toMap(
                        value -> value.formId() + ":" + value.formVersionNo(),
                        value -> value,
                        (first, ignored) -> first));
        Map<String, FmTaskFormRule> taskForms = taskFormRules(version).stream()
                .collect(Collectors.toMap(FmTaskFormRule::getTaskDefKey, value -> value));
        for (FmTaskAssignmentRule rule : assignmentRules(version)) {
            if (!"ACTIVE".equals(rule.getStatus())) {
                continue;
            }
            assignmentRuleConfigValidator.validate(
                    rule.getResolverType(),
                    rule.getResolverConfig(),
                    rule.getFallbackConfig());
            if ("FORM_ACCOUNT_FIELD".equals(rule.getResolverType())) {
                validateFormAccountField(rule, taskForms, forms);
            }
        }
    }

    private void validateFormAccountField(
            FmTaskAssignmentRule rule,
            Map<String, FmTaskFormRule> taskForms,
            Map<String, FmPublishedFormOptionView> forms) throws ServiceException {
        FmTaskFormRule taskForm = taskForms.get(rule.getTaskDefKey());
        if (taskForm == null) {
            throw new ServiceException("User Task「" + rule.getTaskDefKey()
                    + "」尚未選擇表單，無法使用表單選人 Resolver");
        }
        FmPublishedFormOptionView form = forms.get(
                taskForm.getFormId() + ":" + taskForm.getFormVersionNo());
        if (form == null) {
            throw new ServiceException("User Task「" + rule.getTaskDefKey()
                    + "」引用的表單版本不存在或尚未發布");
        }
        try {
            tools.jackson.databind.JsonNode config = new tools.jackson.databind.ObjectMapper()
                    .readTree(rule.getResolverConfig());
            String fieldKey = config.path("fieldKey").asString();
            if (!formFieldCatalog.fields(form.schemaContent()).contains(fieldKey)) {
                throw new ServiceException("User Task「" + rule.getTaskDefKey()
                        + "」的表單選人欄位不存在：" + fieldKey);
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("無法檢查表單選人 Resolver 設定");
        }
    }

    private void validateLevelMatchModesForPublish(FmProcessVersion version)
            throws ServiceException {
        Map<String, String> assignmentModes = taskPolicies(version).stream()
                .collect(Collectors.toMap(
                        FmTaskPolicy::getTaskDefKey,
                        FmTaskPolicy::getAssignmentMode));
        for (FmTaskAssignmentRule rule : assignmentRules(version)) {
            if (!"ACTIVE".equals(rule.getStatus()) || !usesUpToLevel(rule)) {
                continue;
            }
            if (!"SEQUENTIAL".equals(assignmentModes.get(rule.getTaskDefKey()))) {
                throw new ServiceException("User Task「" + rule.getTaskDefKey()
                        + "」使用 UP_TO_LEVEL 時，派送方式必須設定為 SEQUENTIAL");
            }
        }
    }

    private boolean usesUpToLevel(FmTaskAssignmentRule rule) throws ServiceException {
        try {
            tools.jackson.databind.JsonNode config = new tools.jackson.databind.ObjectMapper().readTree(
                    StringUtils.defaultIfBlank(rule.getResolverConfig(), "{}"));
            if ("TARGET_LEVEL_HEAD".equals(rule.getResolverType())) {
                return "UP_TO_LEVEL".equals(
                        config.path("levelMatchMode").asString("EXACT"));
            }
            return false;
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("無法檢查簽核層級匹配模式");
        }
    }

    private void validateGatewayFormFieldsForPublish(FmProcessVersion version)
            throws ServiceException {
        Set<String> gatewayFields = bpmnDesignValidator
                .referencedFormFields(version.getBpmnXml());
        if (gatewayFields.isEmpty()) {
            return;
        }
        Map<String, FmPublishedFormOptionView> publishedForms = taskFormRuleService
                .publishedFormOptions(version.getTenantId()).stream()
                .collect(Collectors.toMap(
                        value -> value.formId() + ":" + value.formVersionNo(),
                        value -> value,
                        (first, ignored) -> first));
        for (FmTaskFormRule rule : taskFormRules(version)) {
            String formKey = rule.getFormId() + ":" + rule.getFormVersionNo();
            FmPublishedFormOptionView form = publishedForms.get(formKey);
            if (form == null) {
                throw new ServiceException("流程綁定的已發佈表單不存在：" + formKey);
            }
            Set<String> missing = new java.util.TreeSet<>(gatewayFields);
            missing.removeAll(formFieldCatalog.fields(form.schemaContent()));
            if (!missing.isEmpty()) {
                throw new ServiceException("Gateway 條件欄位不存在於表單「"
                        + form.label() + "」：" + String.join(", ", missing));
            }
        }
    }

    private String defaultBpmn(FmProcessDef processDef) {
        String key = StringEscapeUtils.escapeXml11(processDef.getProcessKey());
        String name = StringEscapeUtils.escapeXml11(processDef.getProcessName());
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
                  targetNamespace="FlowMint">
                  <process id="%s" name="%s" isExecutable="true">
                    <startEvent id="start" name="開始" />
                    <endEvent id="end" name="結束" />
                    <sequenceFlow id="flow_start_end" sourceRef="start" targetRef="end" />
                  </process>
                  <bpmndi:BPMNDiagram id="diagram">
                    <bpmndi:BPMNPlane id="plane" bpmnElement="%s">
                      <bpmndi:BPMNShape id="start_di" bpmnElement="start">
                        <omgdc:Bounds x="180" y="160" width="36" height="36" />
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNShape id="end_di" bpmnElement="end">
                        <omgdc:Bounds x="360" y="160" width="36" height="36" />
                      </bpmndi:BPMNShape>
                      <bpmndi:BPMNEdge id="flow_start_end_di" bpmnElement="flow_start_end">
                        <omgdi:waypoint x="216" y="178" />
                        <omgdi:waypoint x="360" y="178" />
                      </bpmndi:BPMNEdge>
                    </bpmndi:BPMNPlane>
                  </bpmndi:BPMNDiagram>
                </definitions>
                """.formatted(key, name, key);
    }
    private String sha256(String value) throws ServiceException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new ServiceException("系統不支援 SHA-256");
        }
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
