package org.qifu.fm.domain.runtime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmFormSubmissionValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final ObjectMapper objectMapper;

    public FmFormSubmissionValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(String schemaContent, Map<String, Object> formData)
            throws ServiceException {
        if (StringUtils.isBlank(schemaContent) || formData == null) {
            throw new ServiceException("表單 Schema 或送出資料不可為空");
        }
        JsonNode schema = parse(schemaContent);
        JsonNode components = schema.path("components");
        if (!schema.isObject() || !components.isArray()) {
            throw new ServiceException("已發布表單的 Form.io Schema 格式錯誤");
        }
        List<String> errors = new ArrayList<>();
        validateScope(components, formData, "", errors);
        if (!errors.isEmpty()) {
            throw new ServiceException("表單資料驗證失敗：" + String.join("；", errors));
        }
    }

    private JsonNode parse(String schemaContent) throws ServiceException {
        try {
            return objectMapper.readTree(schemaContent);
        } catch (RuntimeException exception) {
            throw new ServiceException("已發布表單的 Form.io Schema 無法解析");
        }
    }

    private void validateComponents(JsonNode components, Map<String, Object> data,
            String parentPath, List<String> errors) {
        for (JsonNode component : components) {
            validateComponent(component, data, parentPath, errors);
        }
    }

    private void validateScope(JsonNode components, Map<String, Object> data,
            String parentPath, List<String> errors) {
        Set<String> declaredKeys = new java.util.HashSet<>();
        collectScopeKeys(components, declaredKeys);
        for (String key : data.keySet()) {
            if (!declaredKeys.contains(key)) {
                String path = StringUtils.isBlank(parentPath) ? key : parentPath + "." + key;
                errors.add(path + " 不是 Schema 宣告的欄位");
            }
        }
        validateComponents(components, data, parentPath, errors);
    }

    private void collectScopeKeys(JsonNode components, Set<String> keys) {
        for (JsonNode component : components) {
            String key = component.path("key").asText("");
            String type = component.path("type").asText("");
            if (StringUtils.isNotBlank(key) && !isLayout(type)) {
                keys.add(key);
            }
            if (!"container".equals(type)
                    && !"datagrid".equals(type)
                    && !"editgrid".equals(type)) {
                collectScopeKeys(component.path("components"), keys);
                for (JsonNode column : component.path("columns")) {
                    collectScopeKeys(column.path("components"), keys);
                }
                for (JsonNode row : component.path("rows")) {
                    for (JsonNode cell : row) {
                        collectScopeKeys(cell.path("components"), keys);
                    }
                }
            }
        }
    }

    private void validateComponent(JsonNode component, Map<String, Object> data,
            String parentPath, List<String> errors) {
        String key = component.path("key").asText("");
        String type = component.path("type").asText("");
        Object value = StringUtils.isBlank(key) ? null : data.get(key);
        String path = StringUtils.isBlank(parentPath) ? key : parentPath + "." + key;

        if (!isLayout(type) && component.path("input").asBoolean(true)) {
            validateValue(component, value, path, errors);
        }
        if ("container".equals(type) && value instanceof Map<?, ?> nested) {
            validateScope(component.path("components"), stringMap(nested), path, errors);
            return;
        }
        if (("datagrid".equals(type) || "editgrid".equals(type))
                && value instanceof Collection<?> rows) {
            int index = 0;
            for (Object row : rows) {
                if (row instanceof Map<?, ?> nested) {
                    validateScope(component.path("components"), stringMap(nested),
                            path + "[" + index + "]", errors);
                }
                index++;
            }
            return;
        }
        validateNestedLayout(component, data, parentPath, errors);
    }

    private void validateNestedLayout(JsonNode component, Map<String, Object> data,
            String parentPath, List<String> errors) {
        validateComponents(component.path("components"), data, parentPath, errors);
        for (JsonNode column : component.path("columns")) {
            validateComponents(column.path("components"), data, parentPath, errors);
        }
        for (JsonNode row : component.path("rows")) {
            for (JsonNode cell : row) {
                validateComponents(cell.path("components"), data, parentPath, errors);
            }
        }
    }

    private void validateValue(JsonNode component, Object value, String path,
            List<String> errors) {
        JsonNode rules = component.path("validate");
        if (rules.path("required").asBoolean(false) && isEmpty(value)) {
            errors.add(label(component, path) + " 為必填");
            return;
        }
        if (isEmpty(value)) {
            return;
        }
        if (component.path("multiple").asBoolean(false)) {
            if (!(value instanceof Collection<?> values)) {
                errors.add(label(component, path) + " 必須為陣列");
                return;
            }
            for (Object item : values) {
                validateScalar(component, rules, item, path, errors);
            }
            return;
        }
        validateScalar(component, rules, value, path, errors);
    }

    private void validateScalar(JsonNode component, JsonNode rules, Object value,
            String path, List<String> errors) {
        String type = component.path("type").asText("");
        String name = label(component, path);
        if (("number".equals(type) || "currency".equals(type)) && !(value instanceof Number)) {
            errors.add(name + " 必須為數值");
            return;
        }
        if ("checkbox".equals(type) && !(value instanceof Boolean)) {
            errors.add(name + " 必須為布林值");
            return;
        }
        if (isTextType(type) && !(value instanceof String)) {
            errors.add(name + " 必須為文字");
            return;
        }
        if (value instanceof Number number) {
            validateNumber(rules, number, name, errors);
        }
        if (value instanceof String text) {
            validateText(component, rules, text, name, errors);
        }
    }

    private void validateNumber(JsonNode rules, Number value, String name,
            List<String> errors) {
        BigDecimal number = new BigDecimal(value.toString());
        if (rules.has("min") && number.compareTo(rules.path("min").decimalValue()) < 0) {
            errors.add(name + " 不可小於 " + rules.path("min").asText());
        }
        if (rules.has("max") && number.compareTo(rules.path("max").decimalValue()) > 0) {
            errors.add(name + " 不可大於 " + rules.path("max").asText());
        }
    }

    private void validateText(JsonNode component, JsonNode rules, String value,
            String name, List<String> errors) {
        if (rules.has("minLength") && value.length() < rules.path("minLength").asInt()) {
            errors.add(name + " 長度不可少於 " + rules.path("minLength").asInt());
        }
        if (rules.has("maxLength") && value.length() > rules.path("maxLength").asInt()) {
            errors.add(name + " 長度不可超過 " + rules.path("maxLength").asInt());
        }
        String pattern = rules.path("pattern").asText("");
        if (StringUtils.isNotBlank(pattern)) {
            try {
                if (!Pattern.compile(pattern).matcher(value).matches()) {
                    errors.add(name + " 格式不符");
                }
            } catch (PatternSyntaxException exception) {
                errors.add(name + " 的 Schema pattern 無效");
            }
        }
        if ("email".equals(component.path("type").asText())
                && !EMAIL_PATTERN.matcher(value).matches()) {
            errors.add(name + " 不是有效的 Email");
        }
    }

    private boolean isLayout(String type) {
        return List.of("columns", "fieldset", "panel", "table", "tabs", "well")
                .contains(type);
    }

    private boolean isTextType(String type) {
        return List.of("day", "datetime", "email", "input", "password", "phoneNumber",
                "textfield", "textarea", "time", "url").contains(type);
    }

    private boolean isEmpty(Object value) {
        return value == null
                || value instanceof String text && StringUtils.isBlank(text)
                || value instanceof Collection<?> collection && collection.isEmpty();
    }

    private String label(JsonNode component, String path) {
        return component.path("label").asText(StringUtils.defaultIfBlank(path, "未命名欄位"));
    }

    private Map<String, Object> stringMap(Map<?, ?> source) {
        Map<String, Object> result = new java.util.HashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }
}
