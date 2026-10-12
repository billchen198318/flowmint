package org.qifu.fm.domain.authority;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.ObjectMapper;

class FmApprovalAuthorityConditionEvaluatorTest {

    private final FmApprovalAuthorityConditionEvaluator evaluator =
            new FmApprovalAuthorityConditionEvaluator(new ObjectMapper());

    @Test
    void matchesAllNumericAndCategoryConditions() throws Exception {
        String config = """
                {
                  "match": "ALL",
                  "conditions": [
                    {"field": "form.totalAmount", "operator": "GTE", "value": 100000},
                    {"field": "form.category", "operator": "IN",
                     "value": ["MANUFACTURING_EQUIPMENT", "IT_EQUIPMENT"]}
                  ]
                }
                """;
        Map<String, Object> variables = Map.of("form", Map.of(
                "totalAmount", 150000,
                "category", "IT_EQUIPMENT"));

        assertTrue(evaluator.matches(config, variables));
    }

    @Test
    void matchesAnyConditionAndRejectsWhenNoneMatch() throws Exception {
        String config = """
                {
                  "match": "ANY",
                  "conditions": [
                    {"field": "form.days", "operator": "GT", "value": 10},
                    {"field": "form.currency", "operator": "EQ", "value": "USD"}
                  ]
                }
                """;

        assertTrue(evaluator.matches(config,
                Map.of("form", Map.of("days", 3, "currency", "USD"))));
        assertFalse(evaluator.matches(config,
                Map.of("form", Map.of("days", 3, "currency", "TWD"))));
    }

    @Test
    void supportsNotIn() throws Exception {
        String config = """
                {"match":"ALL","conditions":[
                  {"field":"form.category","operator":"NOT_IN","value":["MISC"]}
                ]}
                """;

        assertTrue(evaluator.matches(config,
                Map.of("form", Map.of("category", "IT_EQUIPMENT"))));
        assertFalse(evaluator.matches(config,
                Map.of("form", Map.of("category", "MISC"))));
    }

    @Test
    void matchesNumericFormValueAgainstEditorStringList() throws Exception {
        String config = """
                {"match":"ALL","conditions":[
                  {"field":"form.amount","operator":"IN","value":["100","200"]}
                ]}
                """;
        assertTrue(evaluator.matches(config, Map.of("form", Map.of("amount", 100))));
    }

    @Test
    void rejectsTextValueForNumericComparisonBeforeRuntime() {
        String config = """
                {"match":"ALL","conditions":[
                  {"field":"form.amount","operator":"GT","value":"abc"}
                ]}
                """;
        assertThrows(ServiceException.class, () -> evaluator.validate(config));
    }

    @Test
    void matchesContainerFieldByFullPath() throws Exception {
        String config = """
                {"match":"ALL","conditions":[
                  {"field":"form.details.amount","operator":"GTE","value":100}
                ]}
                """;
        assertTrue(evaluator.matches(config,
                Map.of("form", Map.of("details", Map.of("amount", 100)))));
    }
}
