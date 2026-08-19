package org.qifu.fm.domain.form;

import java.util.HashSet;
import java.util.Set;

import org.qifu.base.exception.ServiceException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class FmFormFieldCatalog {

    private final ObjectMapper objectMapper;

    public FmFormFieldCatalog(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Set<String> fields(String schemaContent) throws ServiceException {
        try {
            Set<String> fields = new HashSet<>();
            collect(objectMapper.readTree(schemaContent).path("components"), fields, false);
            return Set.copyOf(fields);
        } catch (RuntimeException exception) {
            throw new ServiceException("無法解析已發佈表單欄位");
        }
    }

    private void collect(JsonNode components, Set<String> fields, boolean insideGrid) {
        for (JsonNode component : components) {
            String type = component.path("type").asText("");
            String key = component.path("key").asText("");
            if (!insideGrid && !key.isBlank() && component.path("input").asBoolean(true)
                    && !Set.of("columns", "fieldset", "panel", "table", "tabs", "well")
                            .contains(type)) {
                fields.add(key);
            }
            boolean nestedGrid = insideGrid || Set.of("datagrid", "editgrid").contains(type);
            collect(component.path("components"), fields, nestedGrid);
            for (JsonNode column : component.path("columns")) {
                collect(column.path("components"), fields, nestedGrid);
            }
            for (JsonNode row : component.path("rows")) {
                for (JsonNode cell : row) {
                    collect(cell.path("components"), fields, nestedGrid);
                }
            }
        }
    }
}
