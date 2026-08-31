package org.qifu.fm.domain.form;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.ObjectMapper;

class FmFormDesignValidatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FmFormDesignValidator validator = new FmFormDesignValidator();

    @Test
    void runtimePublishingAcceptsOnlyFormioSchema() throws Exception {
        assertDoesNotThrow(() -> validator.validateRuntimeSchema(objectMapper.readTree(
                "{\"display\":\"form\",\"components\":[]}")));
        assertThrows(ServiceException.class, () -> validator.validateRuntimeSchema(
                objectMapper.readTree("{\"type\":\"object\",\"properties\":{}}")));
    }

    @Test
    void acceptsValidNestedFormAndBinding() throws Exception {
        var schema = objectMapper.readTree("""
                {"display":"form","components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"panel","key":"panel","input":false,"components":[
                    {"type":"textfield","key":"actionStatus"}
                  ]},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var uiSchema = objectMapper.readTree("""
                {"engine":"FORMIO","dataActions":[{
                  "bindingId":"binding-1","event":"lookupVendor","actionCode":"VENDOR_LOOKUP",
                  "requestMapping":{"subject":"submission.subject"},
                  "responseMapping":{"status":"subject"},"statusTarget":"actionStatus"
                }]}
                """);

        assertDoesNotThrow(() -> validator.validate(schema, uiSchema));
    }

    @Test
    void rejectsDuplicateAndDangerousKeys() throws Exception {
        var duplicate = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"amount"},
                  {"type":"number","key":"amount"}
                ]}
                """);
        var dangerous = objectMapper.readTree("""
                {"components":[{"type":"textfield","key":"__proto__"}]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(duplicate, objectMapper.createObjectNode()));
        assertThrows(ServiceException.class,
                () -> validator.validate(dangerous, objectMapper.createObjectNode()));
    }

    @Test
    void rejectsEmbeddedExecutableFormioConfiguration() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[{
                  "type":"number","key":"total","calculateValue":"value = data.price * 2"
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, objectMapper.createObjectNode()));
    }

    @Test
    void acceptsDisabledServerCalculationButRejectsEnabledServerCalculation() throws Exception {
        var disabled = objectMapper.readTree("""
                {"components":[{
                  "type":"number","key":"total","calculateServer":false
                }]}
                """);
        var enabled = objectMapper.readTree("""
                {"components":[{
                  "type":"number","key":"total","calculateServer":true
                }]}
                """);

        assertDoesNotThrow(
                () -> validator.validate(disabled, objectMapper.createObjectNode()));
        assertThrows(ServiceException.class,
                () -> validator.validate(enabled, objectMapper.createObjectNode()));
    }

    @Test
    void rejectsBindingToMissingEventOrField() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var missingEvent = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"deletedEvent","actionCode":"LOOKUP"
                }]}
                """);
        var missingField = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "responseMapping":{"name":"vendorName"}
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, missingEvent));
        assertThrows(ServiceException.class,
                () -> validator.validate(schema, missingField));
    }

    @Test
    void rejectsMalformedRequestMapping() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var invalidPath = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "requestMapping":{"subject":{"path":"submission.subject"}}
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, invalidPath));
    }

    @Test
    void acceptsNullRequestMappingAsEmptyMapping() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var uiSchema = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "requestMapping":null
                }]}
                """);

        assertDoesNotThrow(() -> validator.validate(schema, uiSchema));
    }

    @Test
    void rejectsRequestMappingToMissingField() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var uiSchema = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "requestMapping":{"subject":"submission.missingField"}
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, uiSchema));
    }

    @Test
    void rejectsBlankResponseMappingSourcePath() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var uiSchema = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "responseMapping":{"":"subject"}
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, uiSchema));
    }

    @Test
    void rejectsDuplicateResponseMappingTarget() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var uiSchema = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "responseMapping":{"vendorId":"subject","vendorName":"subject"}
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, uiSchema));
    }

    @Test
    void rejectsDataActionTargetsWithConflictingPurposes() throws Exception {
        var schema = objectMapper.readTree("""
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"textfield","key":"status"},
                  {"type":"button","key":"lookup","action":"event","event":"lookupVendor"}
                ]}
                """);
        var responseAndStatus = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "responseMapping":{"vendorName":"subject"},"statusTarget":"subject"
                }]}
                """);
        var statusAndError = objectMapper.readTree("""
                {"dataActions":[{
                  "bindingId":"b1","event":"lookupVendor","actionCode":"LOOKUP",
                  "statusTarget":"status","errorTarget":"status"
                }]}
                """);

        assertThrows(ServiceException.class,
                () -> validator.validate(schema, responseAndStatus));
        assertThrows(ServiceException.class,
                () -> validator.validate(schema, statusAndError));
    }
}
