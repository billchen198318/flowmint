package org.qifu.fm.domain.workflow;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;

import java.util.Set;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class FmAssignmentRuleConfigValidator {

    private static final Set<String> LEVEL_MATCH_MODES = Set.of(
            "EXACT", "EXACT_OR_HIGHER", "UP_TO_LEVEL");

    private final ObjectMapper objectMapper;

    public FmAssignmentRuleConfigValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validate(String resolverType, String resolverConfig, String fallbackConfig)
            throws ServiceException {
        JsonNode config = parseObject(resolverConfig, "Resolver Config");
        switch (resolverType) {
            case "FIXED_ACCOUNT" -> requireTextArray(config, "accounts");
            case "APPROVAL_GROUP" -> requireText(config, "approvalGroupId");
            case "TARGET_LEVEL_HEAD" -> {
                requireText(config, "approvalLevelId");
                validateLevelMatchMode(config);
            }
            case "ORG_TITLE" -> requireText(config, "titleId");
            case "ORG_DUTY" -> requireText(config, "dutyId");
            case "APPROVAL_AUTHORITY" -> requireText(config, "approvalAuthorityId");
            default -> {
                // These resolvers do not require an additional identifier.
            }
        }
        if (StringUtils.isNotBlank(fallbackConfig)) {
            parseObject(fallbackConfig, "Fallback Config");
        }
    }

    private void validateLevelMatchMode(JsonNode config) throws ServiceException {
        JsonNode value = config.path("levelMatchMode");
        if (!value.isMissingNode()
                && (!value.isString() || !LEVEL_MATCH_MODES.contains(value.asString()))) {
            throw new ServiceException(
                    "Resolver Config 的 levelMatchMode 僅允許 EXACT、EXACT_OR_HIGHER 或 UP_TO_LEVEL");
        }
    }

    private JsonNode parseObject(String content, String label) throws ServiceException {
        try {
            JsonNode value = objectMapper.readTree(StringUtils.defaultIfBlank(content, "{}"));
            if (!value.isObject()) {
                throw new ServiceException(label + " 必須是 JSON 物件");
            }
            return value;
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException(label + " JSON 格式錯誤");
        }
    }

    private void requireText(JsonNode config, String field) throws ServiceException {
        if (!config.path(field).isString()
                || StringUtils.isBlank(config.path(field).asString())) {
            throw new ServiceException("Resolver Config 缺少 " + field);
        }
    }

    private void requireTextArray(JsonNode config, String field) throws ServiceException {
        JsonNode values = config.path(field);
        if (!values.isArray() || values.isEmpty()) {
            throw new ServiceException("Resolver Config 缺少 " + field);
        }
        for (JsonNode value : values) {
            if (!value.isString() || StringUtils.isBlank(value.asString())) {
                throw new ServiceException("Resolver Config 的 " + field + " 格式錯誤");
            }
        }
    }
}
