package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.ObjectMapper;

class FmFormSubmissionValidatorTest {

    private static final String SCHEMA = """
            {"components":[
              {"type":"textfield","key":"subject","label":"主旨",
               "validate":{"required":true,"minLength":3,"maxLength":20}},
              {"type":"number","key":"amount","label":"金額",
               "validate":{"required":true,"min":1,"max":10000}},
              {"type":"email","key":"email","label":"Email"},
              {"type":"datagrid","key":"items","label":"明細",
               "validate":{"required":true},"components":[
                {"type":"textfield","key":"name","label":"品名",
                 "validate":{"required":true}}
              ]}
            ]}
            """;

    private final FmFormSubmissionValidator validator =
            new FmFormSubmissionValidator(new ObjectMapper());

    @Test
    void acceptsValidSubmission() {
        Map<String, Object> data = Map.of(
                "subject", "CNC 刀具", "amount", 2500,
                "email", "buyer@example.com",
                "items", List.of(Map.of("name", "鎗刀")));

        assertDoesNotThrow(() -> validator.validate(SCHEMA, data));
    }

    @Test
    void rejectsMissingRequiredField() {
        Map<String, Object> data = Map.of(
                "amount", 2500, "items", List.of(Map.of("name", "鎗刀")));

        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, data));
    }

    @Test
    void rejectsWrongTypeAndOutOfRangeNumber() {
        Map<String, Object> wrongType = Map.of(
                "subject", "CNC", "amount", "2500",
                "items", List.of(Map.of("name", "鎗刀")));
        Map<String, Object> outOfRange = Map.of(
                "subject", "CNC", "amount", 10001,
                "items", List.of(Map.of("name", "鎗刀")));

        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, wrongType));
        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, outOfRange));
    }

    @Test
    void validatesNestedGridRowsAndEmail() {
        Map<String, Object> data = Map.of(
                "subject", "CNC", "amount", 2500, "email", "invalid",
                "items", List.of(Map.of("quantity", 1)));

        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, data));
    }

    @Test
    void rejectsNonObjectGridRowsAndWrongGridContainerType() {
        Map<String, Object> scalarRow = Map.of(
                "subject", "CNC", "amount", 2500,
                "items", List.of("not-an-object"));
        Map<String, Object> wrongGridType = Map.of(
                "subject", "CNC", "amount", 2500,
                "items", "not-an-array");

        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, scalarRow));
        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, wrongGridType));
    }

    @Test
    void rejectsNonObjectContainerValue() {
        String schema = """
                {"components":[
                  {"type":"container","key":"requester","label":"申請人",
                   "components":[{"type":"textfield","key":"account"}]}
                ]}
                """;

        assertThrows(ServiceException.class, () -> validator.validate(
                schema, Map.of("requester", "not-an-object")));
    }

    @Test
    void rejectsInvalidPublishedSchema() {
        assertThrows(ServiceException.class, () -> validator.validate("{}", Map.of()));
        assertThrows(ServiceException.class, () -> validator.validate("not-json", Map.of()));
    }

    @Test
    void rejectsUndeclaredFields() {
        Map<String, Object> data = Map.of(
                "subject", "CNC", "amount", 2500,
                "items", List.of(Map.of("name", "鎗刀", "unitCost", 500)),
                "approvalOverride", true);

        assertThrows(ServiceException.class, () -> validator.validate(SCHEMA, data));
    }

    @Test
    void validatesNativeDateInputAsText() {
        String schema = """
                {"components":[
                  {"type":"input","inputType":"date","key":"requestDate",
                   "label":"申請日期","validate":{"required":true}}
                ]}
                """;

        assertDoesNotThrow(() -> validator.validate(
                schema, Map.of("requestDate", "2026-08-13")));
        assertThrows(ServiceException.class, () -> validator.validate(
                schema, Map.of("requestDate", 20260813)));
    }

    @Test
    void ignoresBlankFormIoValidationLimits() {
        String schema = """
                {"components":[
                  {"type":"textfield","key":"nameZh","label":"中文姓名",
                   "validate":{"required":true,"minLength":"","maxLength":""}},
                  {"type":"number","key":"quantity","label":"印製數量",
                   "validate":{"required":true,"min":"","max":""}}
                ]}
                """;

        assertDoesNotThrow(() -> validator.validate(
                schema, Map.of("nameZh", "王小明", "quantity", 100)));
    }

    @Test
    void reportsMalformedValidationLimitsAsSchemaErrors() {
        String schema = """
                {"components":[
                  {"type":"textfield","key":"subject","label":"主旨",
                   "validate":{"maxLength":"not-a-number"}}
                ]}
                """;

        assertThrows(ServiceException.class, () -> validator.validate(
                schema, Map.of("subject", "名片申請")));
    }
}
