package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.entity.FmProcessSystemTask;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyRuntimeMappingTest {

    private static final String FORM = """
            {"components":[{"type":"number","key":"total","input":true,"validate":{"required":true}}]}
            """;

    @Test
    void inputPreservesDecimalsAndOnlyMapsDeclaredFieldsWithTrustedContext() {
        var binding = binding();
        binding.setInputSchema("""
                {"type":"object","required":["amount","tenant"],"properties":{
                  "amount":{"type":"number"},"tenant":{"type":"string"},
                  "optional":{"type":["string","null"]},"flag":{"type":"boolean"}}}
                """);
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{
                  "amount":{"source":"FORM_DATA","path":"detail.amount"},
                  "tenant":{"source":"PROCESS_CONTEXT","path":"tenantId"},
                  "flag":{"source":"CONSTANT","value":false}},"output":{}}
                """);
        var mapping = new FmGroovyRuntimeMapping(binding);
        var snapshot = mapping.input("""
                {"detail":{"amount":1234567890.123456789},"tenantId":"forged","secret":"not mapped"}
                """, context("T"), 7);
        var input = FmGroovyContractJson.object(snapshot.inputJson());
        assertEquals("1234567890.123456789", input.get("amount").decimalValue().toPlainString());
        assertEquals("T", input.get("tenant").asText());
        assertTrue(input.get("optional").isNull());
        assertFalse(input.get("flag").asBoolean());
        assertFalse(input.has("secret"));
        assertEquals(snapshot, mapping.input("""
                {"detail":{"amount":1234567890.123456789}}
                """, context("T"), 7));
        assertThrows(IllegalArgumentException.class, () -> mapping.input("{}", context("OTHER"), 7));
        assertThrows(IllegalArgumentException.class, () -> mapping.input("{}", context("T"), 7));
    }

    @Test
    void validatesEntireResultAndMergedFormBeforeReturningWritePlan() throws Exception {
        var mapping = new FmGroovyRuntimeMapping(binding());
        var validator = new FmFormSubmissionValidator(JsonMapper.builder().build());
        var result = mapping.apply("{\"total\":1}", "{\"total\":2}", Set.of("total"), FORM, validator);
        assertTrue(result.requiresWrite());
        assertEquals("{\"total\":2}", result.formJson());
        assertThrows(IllegalArgumentException.class, () -> mapping.apply("{\"total\":1}",
                "{\"total\":2}", Set.of(), FORM, validator));
        assertThrows(IllegalArgumentException.class, () -> mapping.apply("{\"total\":1}",
                "{\"total\":2,\"extra\":3}", Set.of("total"), FORM, validator));
        assertThrows(ServiceException.class, () -> mapping.apply("{\"undeclared\":1}",
                "{\"total\":2}", Set.of("total"), FORM, validator));
    }

    @Test
    void optionalAbsentOutputDoesNotEraseSavedValueAndDiscardDoesNotWrite() throws Exception {
        var binding = binding();
        var validator = new FmFormSubmissionValidator(JsonMapper.builder().build());
        var result = new FmGroovyRuntimeMapping(binding).apply("{\"total\":1}", "{}",
                Set.of("total"), FORM, validator);
        assertFalse(result.requiresWrite());
        assertEquals("{\"total\":1}", result.formJson());
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{},"output":{"total":{"target":"DISCARD"}}}
                """);
        result = new FmGroovyRuntimeMapping(binding).apply("{\"total\":1}", "{\"total\":3}",
                Set.of(), FORM, validator);
        assertFalse(result.requiresWrite());
    }

    @Test
    void refusesArrayTraversalAndDoesNotReplaceScalarParent() {
        var binding = binding();
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{},"output":{"total":{"target":"FORM_DATA","path":"detail.total"}}}
                """);
        var mapping = new FmGroovyRuntimeMapping(binding);
        for (String form : new String[] { "{\"detail\":1}", "{\"detail\":[]}" }) {
            assertThrows(IllegalArgumentException.class, () -> mapping.apply(form, "{\"total\":2}",
                    Set.of("detail.total"), FORM, new FmFormSubmissionValidator(JsonMapper.builder().build())));
        }
    }

    @Test
    void rejectsReservedDocumentNumberEvenWithExplicitWritableSet() {
        var binding = binding();
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{},"output":{"total":{"target":"FORM_DATA","path":"documentNumber"}}}
                """);
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyRuntimeMapping(binding));
    }

    static FmProcessSystemTask binding() {
        var binding = new FmProcessSystemTask();
        binding.setTaskType("GROOVY");
        binding.setTenantId("T");
        binding.setProcessDefId("P");
        binding.setVersionNo(1);
        binding.setNodeId("calculate");
        binding.setInputSchema("{\"type\":\"object\",\"properties\":{}}");
        binding.setOutputSchema("{\"type\":\"object\",\"properties\":{\"total\":{\"type\":\"number\"}}}");
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{},"output":{"total":{"target":"FORM_DATA","path":"total"}}}
                """);
        return binding;
    }

    private static FmGroovyRuntimeMapping.TrustedContext context(String tenant) {
        return new FmGroovyRuntimeMapping.TrustedContext(tenant, "P", 1, "instance", "calculate", "invocation",
                Instant.parse("2026-09-10T00:00:00Z"), "business", null, "applicant", "initiator", null);
    }
}
