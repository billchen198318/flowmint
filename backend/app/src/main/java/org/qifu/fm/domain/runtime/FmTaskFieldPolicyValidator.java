package org.qifu.fm.domain.runtime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmTaskFieldPolicyValidator {

    private static final Set<String> POLICIES = Set.of("HIDDEN", "READ", "EDIT", "NONE");

    private final ObjectMapper objectMapper;

    public FmTaskFieldPolicyValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validateConfiguration(String fieldPolicy) throws ServiceException {
        policy(fieldPolicy);
    }

    public void validateChanges(
            String fieldPolicy,
            Map<String, Object> currentData,
            Map<String, Object> submittedData) throws ServiceException {
        JsonNode policy = policy(fieldPolicy);
        String defaultPolicy = normalized(policy.path("default").asString("READ"));
        JsonNode fields = policy.path("fields");
        Set<String> keys = new HashSet<>(currentData.keySet());
        keys.addAll(submittedData.keySet());
        for (String key : keys) {
            String access = normalized(fields.path(key).asString(defaultPolicy));
            if ("EDIT".equals(access)
                    || equivalent(currentData.get(key), submittedData.get(key))) {
                continue;
            }
            if (validateNestedGridChanges(
                    key, currentData.get(key), submittedData.get(key), fields)) {
                continue;
            }
            if (!"EDIT".equals(access)) {
                throw new ServiceException("欄位「" + key + "」不允許在目前關卡修改");
            }
        }
    }

    private boolean validateNestedGridChanges(
            String key, Object currentValue, Object submittedValue, JsonNode fields)
            throws ServiceException {
        String prefix = key + "[].";
        Map<String, String> nestedPolicies = new java.util.LinkedHashMap<>();
        fields.properties().forEach(entry -> {
            if (entry.getKey().startsWith(prefix) && entry.getValue().isString()) {
                nestedPolicies.put(
                        entry.getKey().substring(prefix.length()),
                        normalized(entry.getValue().asString()));
            }
        });
        if (nestedPolicies.isEmpty()
                || !(currentValue instanceof List<?> currentRows)
                || !(submittedValue instanceof List<?> submittedRows)
                || currentRows.size() != submittedRows.size()) {
            return false;
        }
        for (int index = 0; index < currentRows.size(); index++) {
            if (!(currentRows.get(index) instanceof Map<?, ?> currentRow)
                    || !(submittedRows.get(index) instanceof Map<?, ?> submittedRow)) {
                return false;
            }
            Set<Object> rowKeys = new HashSet<>(currentRow.keySet());
            rowKeys.addAll(submittedRow.keySet());
            for (Object rowKeyValue : rowKeys) {
                String rowKey = String.valueOf(rowKeyValue);
                if (equivalent(currentRow.get(rowKeyValue), submittedRow.get(rowKeyValue))) {
                    continue;
                }
                if (!"EDIT".equals(nestedPolicies.get(rowKey))) {
                    throw new ServiceException("欄位「" + key + "[" + index + "]."
                            + rowKey + "」不允許在目前關卡修改");
                }
            }
        }
        return true;
    }

    private boolean equivalent(Object currentValue, Object submittedValue) {
        if (java.util.Objects.deepEquals(currentValue, submittedValue)) {
            return true;
        }
        List<String> currentAttachments = attachmentIds(currentValue);
        List<String> submittedAttachments = attachmentIds(submittedValue);
        return currentAttachments != null
                && submittedAttachments != null
                && currentAttachments.equals(submittedAttachments);
    }

    private List<String> attachmentIds(Object value) {
        if (!(value instanceof List<?> values)) {
            return null;
        }
        List<String> ids = new ArrayList<>();
        for (Object item : values) {
            if (item instanceof String id) {
                ids.add(id);
                continue;
            }
            if (!(item instanceof Map<?, ?> file)) {
                return null;
            }
            Object attachmentId = file.get("attachmentId");
            if (attachmentId instanceof String id && !id.isBlank()) {
                ids.add(id);
                continue;
            }
            Object url = file.get("url");
            String prefix = "#flowmint-attachment-";
            if (url instanceof String text && text.startsWith(prefix)
                    && text.length() > prefix.length()) {
                ids.add(text.substring(prefix.length()));
                continue;
            }
            return null;
        }
        return ids;
    }

    private JsonNode policy(String fieldPolicy) throws ServiceException {
        try {
            JsonNode policy = objectMapper.readTree(fieldPolicy);
            if (!policy.isObject() || !policy.path("fields").isObject()) {
                throw new ServiceException("待辦欄位權限設定格式錯誤");
            }
            String defaultPolicy = normalized(policy.path("default").asString("READ"));
            if (!POLICIES.contains(defaultPolicy)) {
                throw new ServiceException("待辦欄位預設權限不正確");
            }
            var fields = policy.path("fields").properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().isBlank() || !field.getValue().isString()
                        || !POLICIES.contains(normalized(field.getValue().asString()))) {
                    throw new ServiceException("待辦欄位權限設定不正確：" + field.getKey());
                }
            }
            return policy;
        } catch (ServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ServiceException("待辦欄位權限設定格式錯誤");
        }
    }

    private String normalized(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
