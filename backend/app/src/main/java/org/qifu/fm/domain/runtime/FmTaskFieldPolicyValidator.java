package org.qifu.fm.domain.runtime;

import java.util.HashSet;
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
        String defaultPolicy = normalized(policy.path("default").asText("READ"));
        JsonNode fields = policy.path("fields");
        Set<String> keys = new HashSet<>(currentData.keySet());
        keys.addAll(submittedData.keySet());
        for (String key : keys) {
            String access = normalized(fields.path(key).asText(defaultPolicy));
            if (!"EDIT".equals(access)
                    && !java.util.Objects.deepEquals(currentData.get(key), submittedData.get(key))) {
                throw new ServiceException("欄位「" + key + "」不允許在目前關卡修改");
            }
        }
    }

    private JsonNode policy(String fieldPolicy) throws ServiceException {
        try {
            JsonNode policy = objectMapper.readTree(fieldPolicy);
            if (!policy.isObject() || !policy.path("fields").isObject()) {
                throw new ServiceException("待辦欄位權限設定格式錯誤");
            }
            String defaultPolicy = normalized(policy.path("default").asText("READ"));
            if (!POLICIES.contains(defaultPolicy)) {
                throw new ServiceException("待辦欄位預設權限不正確");
            }
            var fields = policy.path("fields").properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().isBlank() || !field.getValue().isTextual()
                        || !POLICIES.contains(normalized(field.getValue().asText()))) {
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
