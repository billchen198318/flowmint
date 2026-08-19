package org.qifu.fm.domain.form;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

@Component
public class FmFormDesignValidator {

    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Set<String> FORBIDDEN_KEYS = Set.of(
            "__proto__", "prototype", "constructor");
    private static final List<String> EXECUTABLE_PROPERTIES = List.of(
            "customDefaultValue", "calculateValue", "calculateServer",
            "customConditional");

    public void validate(JsonNode schema, JsonNode uiSchema) throws ServiceException {
        if (!schema.isObject() || !schema.path("components").isArray()) {
            return;
        }
        Set<String> keys = new HashSet<>();
        Set<String> buttonEvents = new HashSet<>();
        validateComponents(schema.path("components"), keys, buttonEvents, "components");
        validateBindings(uiSchema.path("dataActions"), keys, buttonEvents);
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
            String type = component.path("type").asText("");
            if (StringUtils.isBlank(type)) {
                throw invalid(componentPath + " 缺少 type");
            }
            rejectExecutableConfiguration(component, componentPath);
            String key = component.path("key").asText("");
            if (component.path("input").asBoolean(true) && !isLayout(type)) {
                validateKey(key, componentPath, keys);
            }
            if ("button".equals(type) && "event".equals(component.path("action").asText())) {
                String event = component.path("event").asText("");
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
            if (!value.isMissingNode() && !value.isNull()
                    && (!value.isTextual() || StringUtils.isNotBlank(value.asText()))) {
                throw invalid(path + " 不允許內嵌可執行設定：" + property);
            }
        }
        JsonNode logic = component.path("logic");
        if (logic.isArray() && !logic.isEmpty()) {
            throw invalid(path + " 不允許 Form.io logic；請使用表單客製 JavaScript");
        }
    }

    private void validateBindings(JsonNode bindings, Set<String> keys,
            Set<String> buttonEvents) throws ServiceException {
        if (bindings.isMissingNode() || bindings.isNull()) {
            return;
        }
        if (!bindings.isArray()) {
            throw invalid("UI Schema dataActions 必須是陣列");
        }
        Set<String> bindingIds = new HashSet<>();
        for (JsonNode binding : bindings) {
            String id = binding.path("bindingId").asText("");
            String event = binding.path("event").asText("");
            String actionCode = binding.path("actionCode").asText("");
            if (StringUtils.isAnyBlank(id, event, actionCode) || !bindingIds.add(id)) {
                throw invalid("Data Action Binding 缺少必要資料或 bindingId 重複");
            }
            if (!buttonEvents.contains(event)) {
                throw invalid("Data Action Binding 找不到按鈕事件：" + event);
            }
            validateMappingTargets(binding.path("responseMapping"), keys,
                    "responseMapping");
            validateOptionalTarget(binding.path("statusTarget"), keys, "statusTarget");
            validateOptionalTarget(binding.path("errorTarget"), keys, "errorTarget");
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
        while (properties.hasNext()) {
            String target = properties.next().getValue().asText("");
            if (!keys.contains(target)) {
                throw invalid(name + " 指向不存在的欄位：" + target);
            }
        }
    }

    private void validateOptionalTarget(JsonNode target, Set<String> keys, String name)
            throws ServiceException {
        String value = target.asText("");
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
