package org.qifu.fm.domain.form;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.qifu.base.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

@Component
public class FmFormDesignValidator {

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Pattern SUBMISSION_PATH_PATTERN = Pattern.compile(
            "(?:submission\\.)?[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)*");
    private static final Pattern RESPONSE_PATH_PATTERN = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)*");
    private static final Set<String> FORBIDDEN_KEYS = Set.of(
            "__proto__", "prototype", "constructor");
    private static final Set<String> ALLOWED_ATTACHMENT_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "bmp", "doc", "docx", "xls",
            "xlsx", "ppt", "pptx", "zip", "7z", "rar");
    private static final List<String> EXECUTABLE_PROPERTIES = List.of(
            "customDefaultValue", "calculateValue", "calculateServer",
            "customConditional");

    @Value("${page.maxUploadSize:8388608}")
    private long maxUploadSize = 8L * 1024L * 1024L;

    public void validate(JsonNode schema, JsonNode uiSchema) throws ServiceException {
        if (!schema.isObject() || !schema.path("components").isArray()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        Set<String> buttonEvents = new HashSet<>();
        validateComponents(schema.path("components"), keys, buttonEvents, "components");
        Set<String> fieldPaths = new HashSet<>();
        collectBindingPaths(schema.path("components"), fieldPaths, "");
        validateBindings(uiSchema.path("dataActions"), fieldPaths, buttonEvents);
    }

    private void collectBindingPaths(JsonNode components, Set<String> fields, String parent) {
        for (JsonNode component : components) {
            String type = component.path("type").asString("");
            if (Set.of("datagrid", "editgrid").contains(type)) {
                continue;
            }
            String key = component.path("key").asString("");
            String path = parent.isEmpty() ? key : parent + "." + key;
            if (!key.isBlank() && component.path("input").asBoolean(true)
                    && !isLayout(type) && !"button".equals(type)
                    && !"container".equals(type)) {
                fields.add(path);
            }
            String childParent = "container".equals(type) ? path : parent;
            collectBindingPaths(component.path("components"), fields, childParent);
            for (JsonNode column : component.path("columns")) {
                collectBindingPaths(column.path("components"), fields, childParent);
            }
            for (JsonNode row : component.path("rows")) {
                for (JsonNode cell : row) {
                    collectBindingPaths(cell.path("components"), fields, childParent);
                }
            }
        }
    }

    public void validateRuntimeSchema(JsonNode schema) throws ServiceException {
        if (!schema.isObject()
                || !"form".equals(schema.path("display").asString())
                || !schema.path("components").isArray()) {
            throw invalid("發布只允許 Form.io Schema；請先以表單設計器轉換舊 JSON Schema");
        }
        rejectUnsupportedValidation(schema.path("components"), "components");
    }

    public void validateTaskActionHookSources(JsonNode schema, JsonNode hooks)
            throws ServiceException {
        if (!hooks.isArray()) {
            return;
        }
        Set<String> fields = new HashSet<>();
        collectBindingPaths(schema.path("components"), fields, "");
        for (JsonNode hook : hooks) {
            String hookId = hook.path("hookId").asString("");
            JsonNode mapping = hook.path("requestMapping");
            if (!mapping.isObject()) {
                continue;
            }
            for (Map.Entry<String, JsonNode> entry : mapping.properties()) {
                JsonNode value = entry.getValue();
                String source = value.isString() ? value.asString() : "";
                String formPath = source.startsWith("json:form.")
                        ? source.substring(10)
                        : source.startsWith("form.") ? source.substring(5) : null;
                boolean valid = formPath != null
                        ? fields.contains(formPath)
                        : Set.of("context.taskId", "context.processInstanceId",
                                "context.businessKey").contains(source);
                if (!valid) {
                    throw invalid("Task Action Hook " + hookId
                            + " requestMapping 來源欄位不存在或格式不正確：" + source);
                }
            }
        }
    }

    private void rejectUnsupportedValidation(JsonNode components, String path)
            throws ServiceException {
        int index = 0;
        for (JsonNode component : components) {
            String componentPath = path + "[" + index++ + "]";
            if ("file".equals(component.path("type").asString())) {
                validateAttachmentFileTypes(component.path("fileTypes"), componentPath);
                validateAttachmentFileSize(component.path("fileMaxSize"), componentPath);
                if (!component.path("multiple").asBoolean(false)
                        && component.path("maxNumberOfFiles").asInt(1) > 1) {
                    throw invalid(componentPath
                            + ".maxNumberOfFiles 單檔附件欄位最多只能設定 1 件");
                }
            }
            JsonNode validation = component.path("validate");
            for (String rule : List.of("custom", "json")) {
                JsonNode value = validation.path(rule);
                if (!value.isMissingNode() && !value.isNull()
                        && !(value.isString() && StringUtils.isBlank(value.asString()))
                        && !((value.isObject() || value.isArray()) && value.isEmpty())) {
                    throw invalid(componentPath + ".validate." + rule
                            + " is unsupported by server validation");
                }
            }
            rejectUnsupportedValidation(component.path("components"),
                    componentPath + ".components");
            for (JsonNode column : component.path("columns")) {
                rejectUnsupportedValidation(column.path("components"),
                        componentPath + ".columns.components");
            }
            for (JsonNode row : component.path("rows")) {
                for (JsonNode cell : row) {
                    rejectUnsupportedValidation(cell.path("components"),
                            componentPath + ".rows.components");
                }
            }
        }
    }

    private void validateAttachmentFileTypes(JsonNode fileTypes, String path)
            throws ServiceException {
        if (fileTypes.isMissingNode() || fileTypes.isNull()) return;
        if (!fileTypes.isArray()) {
            throw invalid(path + ".fileTypes 必須是陣列");
        }
        for (JsonNode fileType : fileTypes) {
            String value = fileType.isString()
                    ? fileType.asString() : fileType.path("value").asString("");
            if (StringUtils.isBlank(value)) {
                throw invalid(path + ".fileTypes 不可包含空白類型");
            }
            for (String token : value.trim().toLowerCase(java.util.Locale.ROOT)
                    .split("[, ]+")) {
                String extension = Strings.CS.removeStart(token.trim(), ".");
                if (!ALLOWED_ATTACHMENT_EXTENSIONS.contains(extension)) {
                    throw invalid(path + ".fileTypes 不支援副檔名：" + token);
                }
            }
        }
    }

    private void validateAttachmentFileSize(JsonNode fileMaxSize, String path)
            throws ServiceException {
        String configured = fileMaxSize.asString("");
        long size = 8L * 1024L * 1024L;
        if (StringUtils.isNotBlank(configured)) {
            String normalized = configured.trim().toUpperCase(Locale.ROOT).replace(" ", "");
            try {
                if (normalized.endsWith("MB")) {
                    size = Math.multiplyExact(Long.parseLong(
                            Strings.CS.removeEnd(normalized, "MB")), 1024L * 1024L);
                } else if (normalized.endsWith("KB")) {
                    size = Math.multiplyExact(Long.parseLong(
                            Strings.CS.removeEnd(normalized, "KB")), 1024L);
                } else {
                    size = Long.parseLong(normalized);
                }
            } catch (NumberFormatException | ArithmeticException exception) {
                throw invalid(path + ".fileMaxSize 格式錯誤，請使用 KB 或 MB");
            }
        }
        if (size <= 0 || size > maxUploadSize) {
            throw invalid(path + ".fileMaxSize 必須大於零且不得超過系統上傳上限（"
                    + maxUploadSize + " bytes）");
        }
    }

    private void validateComponents(JsonNode components, Set<String> keys,
            Set<String> buttonEvents, String path) throws ServiceException {
        if (!components.isArray()) {
            throw invalid(path + " 必須是陣列");
        }
        int index = 0;
        for (JsonNode component : components) {
            String componentPath = path + "[" + index + "]";
            if (!component.isObject()) {
                throw invalid(componentPath + " 必須是物件");
            }
            String type = component.path("type").asString("");
            if (StringUtils.isBlank(type)) {
                throw invalid(componentPath + " 缺少 type");
            }
            rejectExecutableConfiguration(component, componentPath);
            String key = component.path("key").asString("");
            if (component.path("input").asBoolean(true) && !isLayout(type)) {
                validateKey(key, componentPath, keys);
            }
            if ("button".equals(type) && "event".equals(component.path("action").asString())) {
                String event = component.path("event").asString("");
                if (StringUtils.isBlank(event) || !buttonEvents.add(event)) {
                    throw invalid(componentPath + " 的按鈕事件為空或重複");
                }
            }
            validateNested(component, keys, buttonEvents, componentPath);
            index++;
        }
    }

    private void validateNested(JsonNode component, Set<String> keys,
            Set<String> buttonEvents, String path) throws ServiceException {
        if (component.has("components")) {
            validateComponents(component.path("components"), keys, buttonEvents,
                    path + ".components");
        }
        int columnIndex = 0;
        for (JsonNode column : component.path("columns")) {
            validateComponents(column.path("components"), keys, buttonEvents,
                    path + ".columns[" + columnIndex++ + "].components");
        }
        int rowIndex = 0;
        for (JsonNode row : component.path("rows")) {
            int cellIndex = 0;
            for (JsonNode cell : row) {
                validateComponents(cell.path("components"), keys, buttonEvents,
                        path + ".rows[" + rowIndex + "][" + cellIndex++ + "].components");
            }
            rowIndex++;
        }
    }

    private void validateKey(String key, String path, Set<String> keys)
            throws ServiceException {
        if (!KEY_PATTERN.matcher(key).matches() || FORBIDDEN_KEYS.contains(key)) {
            throw invalid(path + " 的欄位 key 不合法");
        }
        if (!keys.add(key)) {
            throw invalid("欄位 key 重複：" + key);
        }
    }

    private void rejectExecutableConfiguration(JsonNode component, String path)
            throws ServiceException {
        for (String property : EXECUTABLE_PROPERTIES) {
            JsonNode value = component.path(property);
            if ("calculateServer".equals(property)
                    && value.isBoolean() && !value.asBoolean()) {
                continue;
            }
            if (!value.isMissingNode() && !value.isNull()
                    && (!value.isString() || StringUtils.isNotBlank(value.asString()))) {
                throw invalid(path + " 不允許內嵌可執行設定：" + property);
            }
        }
        JsonNode logic = component.path("logic");
        if (logic.isArray() && !logic.isEmpty()) {
            throw invalid(path + " 不允許 Form.io logic；請使用表單客製 JavaScript");
        }
    }

    private void validateBindings(JsonNode bindings, Set<String> fieldPaths,
            Set<String> buttonEvents) throws ServiceException {
        if (bindings.isMissingNode() || bindings.isNull()) {
            return;
        }
        if (!bindings.isArray()) {
            throw invalid("UI Schema dataActions 必須是陣列");
        }
        Set<String> bindingIds = new HashSet<>();
        for (JsonNode binding : bindings) {
            String id = binding.path("bindingId").asString("");
            String event = binding.path("event").asString("");
            String actionCode = binding.path("actionCode").asString("");
            if (StringUtils.isAnyBlank(id, event, actionCode) || !bindingIds.add(id)) {
                throw invalid("Data Action Binding 缺少必要資料或 bindingId 重複");
            }
            if (!buttonEvents.contains(event)) {
                throw invalid("Data Action Binding 找不到按鈕事件：" + event);
            }
            validateRequestMapping(binding.path("requestMapping"), fieldPaths);
            validateMappingTargets(binding.path("responseMapping"), fieldPaths,
                    "responseMapping");
            validateOptionalTarget(binding.path("statusTarget"), fieldPaths, "statusTarget");
            validateOptionalTarget(binding.path("errorTarget"), fieldPaths, "errorTarget");
            validateBindingTargetCollisions(binding);
        }
    }

    private void validateBindingTargetCollisions(JsonNode binding) throws ServiceException {
        Set<String> responseTargets = new HashSet<>();
        JsonNode responseMapping = binding.path("responseMapping");
        if (responseMapping.isObject()) {
            responseMapping.properties().forEach(
                    property -> responseTargets.add(property.getValue().asString("")));
        }
        String statusTarget = binding.path("statusTarget").asString("");
        String errorTarget = binding.path("errorTarget").asString("");
        if (StringUtils.isNotBlank(statusTarget) && responseTargets.contains(statusTarget)) {
            throw invalid("Data Action target 用途衝突：" + statusTarget
                    + " 同時用於 responseMapping 與 statusTarget");
        }
        if (StringUtils.isNotBlank(errorTarget) && responseTargets.contains(errorTarget)) {
            throw invalid("Data Action target 用途衝突：" + errorTarget
                    + " 同時用於 responseMapping 與 errorTarget");
        }
        if (StringUtils.isNotBlank(statusTarget) && statusTarget.equals(errorTarget)) {
            throw invalid("Data Action target 用途衝突：" + statusTarget
                    + " 同時用於 statusTarget 與 errorTarget");
        }
    }

    private void validateRequestMapping(JsonNode mapping, Set<String> keys)
            throws ServiceException {
        if (mapping.isMissingNode() || mapping.isNull()) {
            return;
        }
        if (!mapping.isObject()) {
            throw invalid("requestMapping 必須是物件");
        }
        Iterator<Map.Entry<String, JsonNode>> properties = mapping.properties().iterator();
        while (properties.hasNext()) {
            Map.Entry<String, JsonNode> property = properties.next();
            if (StringUtils.isBlank(property.getKey())
                    || !property.getValue().isString()
                    || StringUtils.isBlank(property.getValue().asString())) {
                throw invalid("requestMapping 必須使用非空白字串欄位與路徑");
            }
            String path = property.getValue().asString();
            if (!SUBMISSION_PATH_PATTERN.matcher(path).matches()) {
                throw invalid("requestMapping 路徑格式不合法：" + path);
            }
            String normalizedPath = Strings.CS.removeStart(path, "submission.");
            if (!keys.contains(normalizedPath)) {
                throw invalid("requestMapping 指向不存在的欄位：" + path);
            }
        }
    }

    private void validateMappingTargets(JsonNode mapping, Set<String> keys, String name)
            throws ServiceException {
        if (mapping.isMissingNode() || mapping.isNull()) {
            return;
        }
        if (!mapping.isObject()) {
            throw invalid(name + " 必須是物件");
        }
        Iterator<Map.Entry<String, JsonNode>> properties = mapping.properties().iterator();
        Set<String> targets = new HashSet<>();
        while (properties.hasNext()) {
            Map.Entry<String, JsonNode> property = properties.next();
            String source = property.getKey();
            if (!RESPONSE_PATH_PATTERN.matcher(source).matches()) {
                throw invalid(name + " 來源路徑格式不合法：" + source);
            }
            String target = property.getValue().asString("");
            if (!keys.contains(target)) {
                throw invalid(name + " 指向不存在的欄位：" + target);
            }
            if (!targets.add(target)) {
                throw invalid(name + " 目標欄位重複：" + target);
            }
        }
    }

    private void validateOptionalTarget(JsonNode target, Set<String> keys, String name)
            throws ServiceException {
        String value = target.asString("");
        if (StringUtils.isNotBlank(value) && !keys.contains(value)) {
            throw invalid(name + " 指向不存在的欄位：" + value);
        }
    }

    private boolean isLayout(String type) {
        return Set.of("columns", "fieldset", "panel", "table", "tabs", "well")
                .contains(type);
    }

    private ServiceException invalid(String detail) {
        return new ServiceException("表單設計內容不合法：" + detail);
    }
}
