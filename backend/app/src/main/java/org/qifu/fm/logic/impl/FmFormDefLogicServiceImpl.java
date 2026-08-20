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
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.form.FmFormDesignValidator;
import org.qifu.fm.domain.form.FmFormScriptContractValidator;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmFormDefCommand;
import org.qifu.fm.dto.command.FmFormVersionCommand;
import org.qifu.fm.dto.view.FmFormDefView;
import org.qifu.fm.dto.view.FmFormVersionView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.logic.IFmFormDefLogicService;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmDataActionService;
import org.qifu.fm.service.IFmDataActionStepService;
import org.qifu.fm.service.IFmDataActionVersionService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@Transactional(readOnly = true)
public class FmFormDefLogicServiceImpl implements IFmFormDefLogicService {

    private static final String DEFAULT_SCHEMA = """
            {
              "display": "form",
              "components": []
            }
            """;

    private static final String DEFAULT_UI_SCHEMA = """
            {
              "engine": "FORMIO",
              "version": 1
            }
            """;

    private static final String DEFAULT_CUSTOM_SCRIPT = "";

    private final IFmFormDefService formDefService;
    private final IFmFormVersionService formVersionService;
    private final IFmTenantService tenantService;
    private final IFmDataActionService dataActionService;
    private final IFmDataActionVersionService dataActionVersionService;
    private final IFmDataActionStepService dataActionStepService;
    private final ObjectMapper objectMapper;
    private final FmFormDesignValidator formDesignValidator;
    private final FmFormScriptContractValidator formScriptContractValidator;
    private final FmTenantAccessGuard tenantAccessGuard;

    public FmFormDefLogicServiceImpl(
            IFmFormDefService formDefService,
            IFmFormVersionService formVersionService,
            IFmTenantService tenantService,
            IFmDataActionService dataActionService,
            IFmDataActionVersionService dataActionVersionService,
            IFmDataActionStepService dataActionStepService,
            ObjectMapper objectMapper,
            FmFormDesignValidator formDesignValidator,
            FmFormScriptContractValidator formScriptContractValidator,
            FmTenantAccessGuard tenantAccessGuard) {
        this.formDefService = formDefService;
        this.formVersionService = formVersionService;
        this.tenantService = tenantService;
        this.dataActionService = dataActionService;
        this.dataActionVersionService = dataActionVersionService;
        this.dataActionStepService = dataActionStepService;
        this.objectMapper = objectMapper;
        this.formDesignValidator = formDesignValidator;
        this.formScriptContractValidator = formScriptContractValidator;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> create(FmFormDefCommand command)
            throws ServiceException {
        validateMaster(command);
        tenantAccessGuard.requireAccess(command.tenantId());
        assertTenant(command.tenantId());
        assertUnique(command.tenantId(), command.formCode(), null);
        FmFormDef formDef = new FmFormDef();
        formDef.setTenantId(command.tenantId());
        formDef.setFormId(UUID.randomUUID().toString());
        formDef.setFormCode(command.formCode());
        formDef.setFormName(command.formName());
        formDef.setCurrentVersionNo(1);
        formDef.setStatus("DRAFT");
        formDef.setDescription(command.description());
        formDefService.insert(formDef);
        FmFormVersion version = newVersion(formDef, 1, DEFAULT_SCHEMA, DEFAULT_UI_SCHEMA);
        formVersionService.insert(version);
        return load(formDef.getOid(), BaseSystemMessage.insertSuccess());
    }

    @Override
    public DefaultResult<FmFormDefView> load(String oid, String message)
            throws ServiceException {
        FmFormDef formDef = formDefService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(formDef.getTenantId());
        DefaultResult<FmFormDefView> result = success(view(formDef));
        result.setMessage(message);
        return result;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> update(FmFormDefCommand command)
            throws ServiceException {
        FmFormDef formDef = formDefService.selectByPrimaryKey(command.oid())
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(formDef.getTenantId());
        if (!formDef.getTenantId().equals(command.tenantId())) {
            throw new ServiceException("不可變更表單所屬 Tenant");
        }
        if (StringUtils.isBlank(command.formName())) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
        formDef.setFormName(command.formName());
        formDef.setDescription(command.description());
        formDefService.update(formDef);
        return load(formDef.getOid(), BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> deactivate(String oid) throws ServiceException {
        FmFormDef formDef = formDefService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(formDef.getTenantId());
        formDef.setStatus("INACTIVE");
        formDefService.update(formDef);
        return load(oid, BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> saveDraft(FmFormVersionCommand command)
            throws ServiceException {
        FmFormVersion version = draft(command.oid());
        assertFormActive(findDef(version.getTenantId(), version.getFormId()));
        JsonContent content = validateContent(
                command.schemaContent(),
                command.uiSchemaContent(),
                command.customScriptContent());
        version.setSchemaContent(content.schemaContent());
        version.setUiSchemaContent(content.uiSchemaContent());
        version.setCustomScriptContent(content.customScriptContent());
        version.setContentSha256(sha256(content.combined()));
        formVersionService.update(version);
        return loadByBusinessId(
                version.getTenantId(),
                version.getFormId(),
                BaseSystemMessage.updateSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> createVersion(String formDefOid)
            throws ServiceException {
        FmFormDef formDef = formDefService.selectByPrimaryKey(formDefOid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(formDef.getTenantId());
        assertFormActive(formDef);
        List<FmFormVersion> versions = versions(formDef);
        if (versions.stream().anyMatch(value -> "DRAFT".equals(value.getVersionStatus()))) {
            throw new ServiceException("已有草稿版本，請先編輯或發布該版本");
        }
        FmFormVersion source = versions.get(0);
        int nextVersionNo = source.getVersionNo() + 1;
        FmFormVersion version = newVersion(
                formDef,
                nextVersionNo,
                source.getSchemaContent(),
                source.getUiSchemaContent(),
                source.getCustomScriptContent());
        formVersionService.insert(version);
        formDef.setCurrentVersionNo(nextVersionNo);
        formDef.setStatus("DRAFT");
        formDefService.update(formDef);
        return load(formDefOid, BaseSystemMessage.insertSuccess());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmFormDefView> publish(String versionOid) throws ServiceException {
        FmFormVersion version = draft(versionOid);
        JsonContent content = validateContent(
                version.getSchemaContent(),
                version.getUiSchemaContent(),
                version.getCustomScriptContent());
        validatePublishedDataActionBindings(version.getTenantId(), content.uiSchemaContent());
        FmFormDef formDef = findDef(version.getTenantId(), version.getFormId());
        assertFormActive(formDef);
        for (FmFormVersion previous : versions(formDef)) {
            if ("PUBLISHED".equals(previous.getVersionStatus())) {
                previous.setVersionStatus("RETIRED");
                formVersionService.update(previous);
            }
        }
        version.setVersionStatus("PUBLISHED");
        version.setSchemaContent(content.schemaContent());
        version.setUiSchemaContent(content.uiSchemaContent());
        version.setCustomScriptContent(content.customScriptContent());
        version.setContentSha256(sha256(content.combined()));
        version.setPublishedBy(UserUtils.getCurrentUser().getUserId());
        version.setPublishedDate(new Date());
        formVersionService.update(version);
        formDef.setCurrentVersionNo(version.getVersionNo());
        formDef.setStatus("PUBLISHED");
        formDefService.update(formDef);
        return load(formDef.getOid(), "表單版本發布成功");
    }

    @Override
    public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("status", "ACTIVE");
        var tenantIds = UserUtils.isAdmin() ? null : tenantAccessGuard.accessibleTenantIds();
        return success(tenantService.selectListByParams(parameters, "TENANT_CODE", "ASC")
                .getValue().stream()
                .filter(value -> tenantIds == null || tenantIds.contains(value.getTenantId()))
                .map(value -> new FmOptionView(
                        value.getTenantId(),
                        value.getTenantCode() + "：" + value.getTenantName()))
                .toList());
    }

    private FmFormDefView view(FmFormDef formDef) throws ServiceException {
        List<FmFormVersionView> versionViews = versions(formDef).stream()
                .map(value -> new FmFormVersionView(
                        value.getOid(),
                        value.getVersionNo(),
                        value.getVersionStatus(),
                        value.getSchemaContent(),
                        value.getUiSchemaContent(),
                        value.getCustomScriptContent(),
                        value.getContentSha256(),
                        value.getPublishedBy(),
                        value.getPublishedDate()))
                .toList();
        return new FmFormDefView(
                formDef.getOid(),
                formDef.getTenantId(),
                formDef.getFormId(),
                formDef.getFormCode(),
                formDef.getFormName(),
                formDef.getCurrentVersionNo(),
                formDef.getStatus(),
                formDef.getDescription(),
                versionViews);
    }

    private void validateMaster(FmFormDefCommand command) throws ServiceException {
        if (StringUtils.isAnyBlank(
                command.tenantId(), command.formCode(), command.formName())
                || !command.formCode().matches("[A-Za-z][A-Za-z0-9_-]*")) {
            throw new ServiceException(BaseSystemMessage.parameterIncorrect());
        }
    }

    private void assertTenant(String tenantId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("status", "ACTIVE");
        if (tenantService.selectListByParams(parameters).getValue().isEmpty()) {
            throw new ServiceException("Tenant 不存在或已停用");
        }
    }

    private void assertUnique(String tenantId, String formCode, String excludedOid)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formCode", formCode);
        boolean exists = formDefService.selectListByParams(parameters).getValue().stream()
                .anyMatch(value -> !value.getOid().equals(excludedOid));
        if (exists) {
            throw new ServiceException("同一 Tenant 的表單代碼不可重複");
        }
    }

    private void assertFormActive(FmFormDef formDef) throws ServiceException {
        if ("INACTIVE".equals(formDef.getStatus())) {
            throw new ServiceException("已停用表單不可修改或發布版本");
        }
    }

    private FmFormVersion draft(String oid) throws ServiceException {
        FmFormVersion version = formVersionService.selectByPrimaryKey(oid)
                .getValueEmptyThrowMessage();
        tenantAccessGuard.requireAccess(version.getTenantId());
        if (!"DRAFT".equals(version.getVersionStatus())) {
            throw new ServiceException("已發布或已退役版本不可修改");
        }
        return version;
    }

    private List<FmFormVersion> versions(FmFormDef formDef) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", formDef.getTenantId());
        parameters.put("formId", formDef.getFormId());
        return formVersionService.selectListByParams(
                parameters, "VERSION_NO", "DESC").getValue();
    }

    private FmFormDef findDef(String tenantId, String formId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        return formDefService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException(BaseSystemMessage.dataNoExist()));
    }

    private DefaultResult<FmFormDefView> loadByBusinessId(
            String tenantId,
            String formId,
            String message) throws ServiceException {
        return load(findDef(tenantId, formId).getOid(), message);
    }

    private FmFormVersion newVersion(
            FmFormDef formDef,
            Integer versionNo,
            String schemaContent,
            String uiSchemaContent) throws ServiceException {
        return newVersion(
                formDef,
                versionNo,
                schemaContent,
                uiSchemaContent,
                DEFAULT_CUSTOM_SCRIPT);
    }

    private FmFormVersion newVersion(
            FmFormDef formDef,
            Integer versionNo,
            String schemaContent,
            String uiSchemaContent,
            String customScriptContent) throws ServiceException {
        JsonContent content = validateContent(
                schemaContent,
                uiSchemaContent,
                customScriptContent);
        FmFormVersion version = new FmFormVersion();
        version.setTenantId(formDef.getTenantId());
        version.setFormId(formDef.getFormId());
        version.setVersionNo(versionNo);
        version.setVersionStatus("DRAFT");
        version.setSchemaContent(content.schemaContent());
        version.setUiSchemaContent(content.uiSchemaContent());
        version.setCustomScriptContent(content.customScriptContent());
        version.setContentSha256(sha256(content.combined()));
        return version;
    }

    private JsonContent validateContent(
            String schemaContent,
            String uiSchemaContent,
            String customScriptContent) throws ServiceException {
        if (StringUtils.isAnyBlank(schemaContent, uiSchemaContent)) {
            throw new ServiceException("JSON Schema 與 UI Schema 不可空白");
        }
        try {
            JsonNode schema = objectMapper.readTree(schemaContent);
            JsonNode uiSchema = objectMapper.readTree(uiSchemaContent);
            boolean jsonSchema = schema.isObject()
                    && "object".equals(schema.path("type").asString())
                    && (!schema.has("properties") || schema.path("properties").isObject());
            boolean formioSchema = schema.isObject()
                    && "form".equals(schema.path("display").asString())
                    && schema.path("components").isArray();
            if (!jsonSchema && !formioSchema) {
                throw new ServiceException("表單內容必須是有效的 Form.io 或 JSON Schema 格式");
            }
            if (!uiSchema.isObject()) {
                throw new ServiceException("UI Schema 根節點必須是 JSON 物件");
            }
            if (formioSchema) {
                if (!"FORMIO".equals(uiSchema.path("engine").asString())) {
                    throw new ServiceException("Form.io 表單缺少正確的引擎識別");
                }
                formDesignValidator.validate(schema, uiSchema);
            }
            String normalizedSchema = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(schema);
            String normalizedUiSchema = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(uiSchema);
            String normalizedScript = normalizeScript(customScriptContent);
            formScriptContractValidator.validate(normalizedScript);
            return new JsonContent(
                    normalizedSchema,
                    normalizedUiSchema,
                    normalizedScript);
        } catch (JacksonException exception) {
            throw new ServiceException("表單 JSON 格式錯誤：" + exception.getMessage());
        }
    }

    private String normalizeScript(String customScriptContent) {
        return StringUtils.defaultString(customScriptContent)
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private void validatePublishedDataActionBindings(String tenantId, String uiSchemaContent)
            throws ServiceException {
        try {
            JsonNode bindings = objectMapper.readTree(uiSchemaContent).path("dataActions");
            if (!bindings.isArray()) {
                return;
            }
            for (JsonNode binding : bindings) {
                String bindingId = binding.path("bindingId").asText("");
                String actionCode = binding.path("actionCode").asText("");
                FmDataAction action = findPublishedDataAction(tenantId, actionCode, bindingId);
                Integer versionNo = binding.hasNonNull("actionVersion")
                        ? binding.path("actionVersion").asInt(0)
                        : action.getCurrentVersionNo();
                if (versionNo == null || versionNo <= 0) {
                    throw bindingInvalid(bindingId, "actionVersion 必須是正整數");
                }
                assertPublishedDataActionVersion(tenantId, action, versionNo, bindingId);
                validateDataActionMetadata(tenantId, action, versionNo, binding, bindingId);
            }
        } catch (JacksonException exception) {
            throw new ServiceException("表單 Data Action Binding JSON 格式錯誤："
                    + exception.getMessage());
        }
    }

    private FmDataAction findPublishedDataAction(String tenantId, String actionCode,
            String bindingId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("actionCode", actionCode);
        parameters.put("status", "ACTIVE");
        return dataActionService.selectListByParams(parameters).getValue().stream()
                .findFirst()
                .orElseThrow(() -> bindingInvalid(bindingId,
                        "找不到同 Tenant 的啟用 Data Action：" + actionCode));
    }

    private void assertPublishedDataActionVersion(String tenantId, FmDataAction action,
            int versionNo, String bindingId) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("actionId", action.getActionId());
        parameters.put("versionNo", versionNo);
        parameters.put("versionStatus", "PUBLISHED");
        if (dataActionVersionService.selectListByParams(parameters).getValue().isEmpty()) {
            throw bindingInvalid(bindingId, "Data Action " + action.getActionCode()
                    + " Version " + versionNo + " 不存在或尚未發布");
        }
    }

    private void validateDataActionMetadata(String tenantId, FmDataAction action,
            int versionNo, JsonNode binding, String bindingId) throws ServiceException {
        Set<String> requestFields;
        try {
            JsonNode requestSchema = objectMapper.readTree(
                    StringUtils.defaultIfBlank(action.getRequestSchema(), "{}"));
            if (!requestSchema.isObject()) {
                throw bindingInvalid(bindingId, "Data Action request schema 不是物件");
            }
            requestFields = requestSchema.propertyNames().stream().collect(Collectors.toSet());
        } catch (JacksonException exception) {
            throw bindingInvalid(bindingId, "Data Action request schema 格式錯誤");
        }
        Set<String> mappedRequestFields = binding.path("requestMapping").isObject()
                ? binding.path("requestMapping").propertyNames().stream().collect(Collectors.toSet())
                : Set.of();
        if (!requestFields.equals(mappedRequestFields)) {
            throw bindingInvalid(bindingId, "requestMapping 欄位與 Data Action metadata 不一致");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("actionId", action.getActionId());
        parameters.put("versionNo", versionNo);
        parameters.put("status", "ACTIVE");
        Set<String> responseKeys = dataActionStepService.selectListByParams(parameters)
                .getValue().stream()
                .filter(step -> !"NONE".equals(step.getResultMode()))
                .map(FmDataActionStep::getResultKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        for (String sourcePath : binding.path("responseMapping").propertyNames()) {
            String rootKey = StringUtils.substringBefore(sourcePath, ".");
            if (!responseKeys.contains(rootKey)) {
                throw bindingInvalid(bindingId, "responseMapping 來源不在 Data Action metadata："
                        + sourcePath);
            }
        }
    }

    private ServiceException bindingInvalid(String bindingId, String detail) {
        return new ServiceException("Data Action Binding " + bindingId + "：" + detail);
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

    private record JsonContent(
            String schemaContent,
            String uiSchemaContent,
            String customScriptContent) {

        private String combined() {
            return schemaContent + "\n" + uiSchemaContent + "\n" + customScriptContent;
        }
    }
}
