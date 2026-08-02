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
import org.qifu.fm.dto.command.FmProcessVersionCommand;
import org.qifu.fm.dto.command.FmTaskFormRuleCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmProcessDefView;
import org.qifu.fm.dto.view.FmProcessVersionView;
import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.dto.view.FmTaskFormRuleView;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.logic.IFmProcessDefLogicService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessDefLogicServiceImpl implements IFmProcessDefLogicService {

    private final IFmProcessDefService processDefService;
    private final IFmProcessVersionService processVersionService;
    private final IFmTenantService tenantService;
    private final IFmTaskFormRuleService taskFormRuleService;
    private final RepositoryService repositoryService;

    public FmProcessDefLogicServiceImpl(
            IFmProcessDefService processDefService,
            IFmProcessVersionService processVersionService,
            IFmTenantService tenantService,
            IFmTaskFormRuleService taskFormRuleService,
            RepositoryService repositoryService) {
        this.processDefService = processDefService;
        this.processVersionService = processVersionService;
        this.tenantService = tenantService;
        this.taskFormRuleService = taskFormRuleService;
        this.repositoryService = repositoryService;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> create(FmProcessDefCommand command)
            throws ServiceException {
        validate(command);
        assertUnique(command.tenantId(), command.processKey(), null);
        FmProcessDef processDef = new FmProcessDef();
        processDef.setTenantId(command.tenantId());
        processDef.setProcessDefId(UUID.randomUUID().toString());
        processDef.setProcessKey(command.processKey());
        processDef.setProcessName(command.processName());
        processDef.setCategory(command.category());
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
        if (StringUtils.isBlank(command.processName())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        processDef.setProcessName(command.processName());
        processDef.setCategory(command.category());
        processDef.setDescription(command.description());
        processDefService.update(processDef);
        return load(processDef.getOid(), BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmProcessDefView> deactivate(String oid) throws ServiceException {
        FmProcessDef processDef = processDefService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
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
        saveTaskForms(version, command.taskForms(), userTaskKeys(command.bpmnXml()));
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
        validateTaskFormsForPublish(version, userTaskKeys(version.getBpmnXml()));
        String resourceName = processDef.getProcessKey() + "-v" + version.getVersionNo()
                + ".bpmn20.xml";
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .tenantId(version.getTenantId())
                    .name(processDef.getProcessName() + " v" + version.getVersionNo())
                    .addString(resourceName, version.getBpmnXml()).deploy();
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
        return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue()
                .stream().map(value -> new FmOptionView(value.getTenantId(),
                        value.getTenantCode() + "：" + value.getTenantName())).toList());
    }

    @Override
    public DefaultResult<List<FmPublishedFormOptionView>> publishedFormOptions(String tenantId)
            throws ServiceException {
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        return success(taskFormRuleService.publishedFormOptions(tenantId));
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
                                rule.getFormVersionNo())).toList())).toList();
        return new FmProcessDefView(processDef.getOid(), processDef.getTenantId(),
                processDef.getProcessDefId(), processDef.getProcessKey(),
                processDef.getProcessName(), processDef.getCategory(),
                processDef.getCurrentVersionNo(), processDef.getStatus(),
                processDef.getDescription(), versionViews);
    }

    private void validate(FmProcessDefCommand command) throws ServiceException {
        if (StringUtils.isAnyBlank(command.tenantId(), command.processKey(),
                command.processName()) || !command.processKey().matches("[A-Za-z][A-Za-z0-9_-]*")) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
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
        try {
            javax.xml.stream.XMLStreamReader reader = javax.xml.stream.XMLInputFactory
                    .newFactory().createXMLStreamReader(new java.io.StringReader(xml));
            org.flowable.bpmn.model.BpmnModel model =
                    new org.flowable.bpmn.converter.BpmnXMLConverter()
                            .convertToBpmnModel(reader);
            if (model.getProcesses().size() != 1
                    || !expectedProcessKey.equals(model.getMainProcess().getId())) {
                throw new ServiceException("BPMN 必須只有一個 Process，且 Process ID 必須等於流程代碼 "
                        + expectedProcessKey);
            }            if (!model.getMainProcess().findFlowElementsOfType(
                    org.flowable.bpmn.model.ScriptTask.class).isEmpty()) {
                throw new ServiceException("首版流程禁止使用 Script Task");
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("BPMN XML 驗證失敗：" + exception.getMessage());
        }
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
            FmTaskFormRule previous = existing.get(command.taskDefKey());
            if (previous != null && command.formId().equals(previous.getFormId())
                    && command.formVersionNo().equals(previous.getFormVersionNo())) {
                rule.setFieldPolicy(previous.getFieldPolicy());
            }
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
        if (!taskKeys.isEmpty()) {
            throw new ServiceException("User Task 表單設定已通過；請先由 FM_PROG004D0002 完成 Task Policy 與簽核人設定後再發布");
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
