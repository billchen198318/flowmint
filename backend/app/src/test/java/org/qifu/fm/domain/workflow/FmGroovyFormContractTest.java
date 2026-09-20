package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

class FmGroovyFormContractTest {

    @Test
    void readonlyCalculationFieldRequiresExplicitSystemCapability() {
        var binding = FmGroovyRuntimeMappingTest.binding();
        var schema = """
                {"components":[{"type":"number","key":"total","disabled":true,
                  "properties":{"flowmintSystemTaskWritable":"true"}}]}
                """;
        assertEquals(Set.of("total"), new FmGroovyFormContract(schema).validate(binding));
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyFormContract(
                schema.replace("\"flowmintSystemTaskWritable\":\"true\"", "\"other\":true")).validate(binding));
    }

    @Test
    void rejectsNonPersistentFieldsUnknownTypesAndWrongOutputType() {
        var binding = FmGroovyRuntimeMappingTest.binding();
        for (String component : new String[] {
                "\"type\":\"number\",\"persistent\":false", "\"type\":\"number\",\"persistent\":\"client-only\"",
                "\"type\":\"file\"", "\"type\":\"hidden\"", "\"type\":\"textfield\"" }) {
            String schema = "{\"components\":[{\"key\":\"total\",\"properties\":{\"flowmintSystemTaskWritable\":true},"
                    + component + "}]}";
            assertThrows(IllegalArgumentException.class, () -> new FmGroovyFormContract(schema).validate(binding));
        }
    }

    @Test
    void rejectsDuplicateLayoutKeysAndProtectedParentWrites() {
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyFormContract("""
                {"components":[{"type":"number","key":"total"},{"type":"panel","components":[
                  {"type":"number","key":"total"}]}]}
                """));
        var binding = FmGroovyRuntimeMappingTest.binding();
        binding.setMappingContent("""
                {"mappingVersion":1,"input":{},"output":{"total":{"target":"FORM_DATA","path":"tenantId.total"}}}
                """);
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyFormContract("""
                {"components":[{"type":"container","key":"tenantId","components":[
                  {"type":"number","key":"total","properties":{"flowmintSystemTaskWritable":true}}]}]}
                """).validate(binding));
    }
}
