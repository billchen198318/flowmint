package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.dto.external.FmExternalFormTemplateView;

import tools.jackson.databind.ObjectMapper;

class FmExternalFormContractTest {
    @Test
    void describesNestedObjectsArraysAndFixedChoices() throws Exception {
        var schema = new ObjectMapper().readTree("""
                {"components":[
                  {"type":"container","key":"details","components":[
                    {"type":"number","key":"quantity","input":true,"validate":{"required":true}}]},
                  {"type":"datagrid","key":"items","components":[
                    {"type":"textfield","key":"name","input":true},
                    {"type":"select","key":"state","input":true,
                     "data":{"values":[{"value":"NEW"},{"value":"DONE"}]}}]},
                  {"type":"columns","key":"layout","columns":[{"components":[
                    {"type":"checkbox","key":"approved","input":true}]}]},
                  {"type":"textfield","key":"tenantId","input":true}
                ]}
                """);
        FmExternalDesignQueryService service = new FmExternalDesignQueryService(
                null, null, null, null, null, new ObjectMapper());
        List<FmExternalFormTemplateView.FieldContract> fields = new ArrayList<>();
        List<FmExternalFormTemplateView.AttachmentField> attachments = new ArrayList<>();

        service.collectFields(schema.path("components"), fields, attachments, "");

        assertEquals(List.of("details", "details.quantity", "items", "items[].name",
                "items[].state", "approved"), fields.stream().map(
                        FmExternalFormTemplateView.FieldContract::path).toList());
        assertEquals("object", fields.get(0).valueType());
        assertEquals("number", fields.get(1).valueType());
        assertTrue(fields.get(1).required());
        assertEquals("array", fields.get(2).valueType());
        assertEquals("object", fields.get(2).itemType());
        assertEquals(List.of("NEW", "DONE"), fields.get(4).enumValues().stream()
                .map(node -> node.asText()).toList());
        assertEquals("boolean", fields.get(5).valueType());
        assertFalse(fields.stream().anyMatch(field -> "tenantId".equals(field.key())));
    }
}
