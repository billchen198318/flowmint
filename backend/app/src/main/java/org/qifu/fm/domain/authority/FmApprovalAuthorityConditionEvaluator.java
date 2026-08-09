package org.qifu.fm.domain.authority;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmApprovalAuthorityConditionEvaluator {

    private static final List<String> MATCH_MODES = List.of("ALL", "ANY");
    private static final List<String> OPERATORS = List.of(
            "EQ", "NE", "GT", "GTE", "LT", "LTE", "IN", "NOT_IN");

    private final ObjectMapper objectMapper;

    public FmApprovalAuthorityConditionEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean matches(String conditionConfig, Map<String, Object> variables)
            throws ServiceException {
        JsonNode config = parse(conditionConfig);
        String matchMode = config.path("match").asString("ALL");
        JsonNode conditions = config.path("conditions");
        if (!MATCH_MODES.contains(matchMode) || !conditions.isArray()
                || conditions.size() == 0) {
            throw new ServiceException("核決權限條件格式不正確");
        }
        boolean anyMatched = false;
        for (JsonNode condition : conditions) {
            boolean matched = evaluate(condition, variables);
            if ("ALL".equals(matchMode) && !matched) {
                return false;
            }
            anyMatched |= matched;
        }
        return "ALL".equals(matchMode) || anyMatched;
    }

    private boolean evaluate(JsonNode condition, Map<String, Object> variables)
            throws ServiceException {
        String field = condition.path("field").asString();
        String operator = condition.path("operator").asString();
        JsonNode expected = condition.path("value");
        if (StringUtils.isBlank(field) || !OPERATORS.contains(operator)
                || expected.isMissingNode()) {
            throw new ServiceException("核決權限條件項目不正確");
        }
        Object actual = valueByPath(variables, field);
        return switch (operator) {
            case "EQ" -> equalsValue(actual, expected);
            case "NE" -> !equalsValue(actual, expected);
            case "GT" -> compareNumber(actual, expected) > 0;
            case "GTE" -> compareNumber(actual, expected) >= 0;
            case "LT" -> compareNumber(actual, expected) < 0;
            case "LTE" -> compareNumber(actual, expected) <= 0;
            case "IN" -> contains(expected, actual);
            case "NOT_IN" -> !contains(expected, actual);
            default -> false;
        };
    }

    private Object valueByPath(Map<String, Object> variables, String path) {
        Object current = variables;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment);
        }
        return current;
    }

    private boolean equalsValue(Object actual, JsonNode expected) {
        if (actual == null) {
            return expected.isNull();
        }
        if (actual instanceof Number) {
            try {
                return new BigDecimal(actual.toString()).compareTo(expected.decimalValue()) == 0;
            } catch (RuntimeException exception) {
                return false;
            }
        }
        if (actual instanceof Boolean booleanValue) {
            return expected.isBoolean() && booleanValue == expected.booleanValue();
        }
        return actual.toString().equals(expected.asString());
    }

    private int compareNumber(Object actual, JsonNode expected) throws ServiceException {
        try {
            return new BigDecimal(String.valueOf(actual)).compareTo(expected.decimalValue());
        } catch (RuntimeException exception) {
            throw new ServiceException("核決權限數值條件無法比較");
        }
    }

    private boolean contains(JsonNode expected, Object actual) throws ServiceException {
        if (!expected.isArray()) {
            throw new ServiceException("IN 條件的 value 必須是陣列");
        }
        for (JsonNode item : expected) {
            if (equalsValue(actual, item)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode parse(String conditionConfig) throws ServiceException {
        try {
            return objectMapper.readTree(conditionConfig);
        } catch (RuntimeException exception) {
            throw new ServiceException("核決權限條件不是有效的 JSON");
        }
    }
}
