package org.qifu.fm.domain.form;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmFormFieldCatalogTest {

    @Test
    void collectsFieldsFromNestedLayoutsAndGrids() throws Exception {
        String schema = """
                {"components":[
                  {"type":"textfield","key":"subject"},
                  {"type":"panel","key":"layout","input":false,"components":[
                    {"type":"number","key":"totalAmount"}
                  ]},
                  {"type":"datagrid","key":"items","components":[
                    {"type":"textfield","key":"itemName"}
                  ]}
                ]}
                """;

        assertEquals(Set.of("subject", "totalAmount", "items"),
                new FmFormFieldCatalog(new ObjectMapper()).fields(schema));
    }
}
