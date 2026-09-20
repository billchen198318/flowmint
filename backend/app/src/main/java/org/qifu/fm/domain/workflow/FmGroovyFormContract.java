package org.qifu.fm.domain.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.qifu.fm.entity.FmProcessSystemTask;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** Publish-time Form.io field contract, separate from User Task edit permissions. */
public final class FmGroovyFormContract {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Set<String> LAYOUT = Set.of("panel", "fieldset", "columns", "table", "tabs", "well");
    private static final Set<String> PROTECTED = Set.of("tenantId", "applicantAccount", "initiatorAccount",
            "applicantOrgUnitId", "documentNo", "documentNumber", "businessKey", "processInstanceId",
            "processStatus", "versionNo", "formDataId", "formVersionNo", "revisionNo", "lockVersion", "attachmentIds");
    private final Map<String, Field> fields = new HashMap<>();

    public FmGroovyFormContract(String formSchema) {
        JsonNode schema = FmGroovyContractJson.object(formSchema);
        if (!schema.path("components").isArray()) {
            throw new IllegalArgumentException("GROOVY_FORM_SCHEMA_INVALID");
        }
        collect(schema.get("components"), "", true);
    }

    public Set<String> validate(FmProcessSystemTask binding) {
        JsonNode input = FmGroovyContractJson.object(binding.getInputSchema());
        JsonNode output = FmGroovyContractJson.object(binding.getOutputSchema());
        JsonNode mapping = FmGroovyContractJson.object(binding.getMappingContent());
        FmGroovyContractJson.validateSchema(input);
        FmGroovyContractJson.validateSchema(output);
        FmGroovyDraftValidator.validateMapping(mapping, input, output);
        for (String name : mapping.path("input").propertyNames()) {
            JsonNode entry = mapping.path("input").get(name);
            if ("FORM_DATA".equals(entry.path("source").asText())) {
                Field field = field(entry.path("path").asText());
                compatible(field.schema(), input.path("properties").get(name));
            } else if ("PROCESS_CONTEXT".equals(entry.path("source").asText())) {
                String context = entry.path("path").asText();
                boolean nullable = Set.of("documentNo", "applicantOrgUnitId").contains(context);
                compatible(type("processVersionNo".equals(context) ? "integer" : "string", nullable),
                        input.path("properties").get(name));
            }
        }
        Set<String> writable = new HashSet<>();
        for (String name : mapping.path("output").propertyNames()) {
            JsonNode entry = mapping.path("output").get(name);
            if ("DISCARD".equals(entry.path("target").asText())) {
                continue;
            }
            String path = entry.path("path").asText();
            Field field = field(path);
            if (!field.writable()) {
                throw new IllegalArgumentException("GROOVY_FORM_WRITE_DENIED");
            }
            compatible(output.path("properties").get(name), field.schema());
            writable.add(path);
        }
        return Set.copyOf(writable);
    }

    private Field field(String path) {
        Field field = fields.get(path);
        if (field == null || !field.persistent() || field.schema() == null) {
            throw new IllegalArgumentException("GROOVY_FORM_FIELD_UNSUPPORTED");
        }
        return field;
    }

    private ObjectNode collect(JsonNode components, String prefix, boolean parentPersistent) {
        ObjectNode properties = JSON.createObjectNode();
        for (JsonNode component : components) {
            String componentType = component.path("type").asText();
            if (LAYOUT.contains(componentType)) {
                merge(properties, collect(component.path("components"), prefix, parentPersistent));
                for (JsonNode column : component.path("columns")) {
                    merge(properties, collect(column.path("components"), prefix, parentPersistent));
                }
                for (JsonNode row : component.path("rows")) {
                    for (JsonNode cell : row) {
                        merge(properties, collect(cell.path("components"), prefix, parentPersistent));
                    }
                }
                continue;
            }
            String key = component.path("key").asText();
            if (!key.matches("[A-Za-z][A-Za-z0-9_]*") || !component.path("input").asBoolean(true)) {
                continue;
            }
            String path = prefix + key;
            JsonNode persistence = component.get("persistent");
            boolean persistent = parentPersistent && (persistence == null || persistence.isBoolean()
                    && persistence.asBoolean() || "server".equals(persistence.asText()));
            boolean nullable = !component.path("validate").path("required").asBoolean(false);
            ObjectNode schema = switch (componentType) {
                case "textfield", "textarea", "email", "url", "phoneNumber" -> type("string", nullable);
                case "number", "currency" -> type("number", nullable);
                case "checkbox" -> type("boolean", nullable);
                case "container" -> {
                    ObjectNode value = type("object", nullable);
                    value.set("properties", collect(component.path("components"), path + ".", persistent));
                    yield value;
                }
                default -> null;
            };
            if (component.path("multiple").asBoolean(false)) {
                schema = null;
            }
            // Hidden/select value types cannot be inferred safely from their labels or widget.
            if (Set.of("hidden", "select", "selectboxes", "datagrid", "editgrid").contains(componentType)
                    && component.path("properties").path("flowmintGroovySchema").isString()) {
                schema = (ObjectNode) FmGroovyContractJson.object(
                        component.path("properties").path("flowmintGroovySchema").asText());
                FmGroovyContractJson.validateSchema(schema);
            }
            JsonNode capability = component.path("properties").path("flowmintSystemTaskWritable");
            boolean writable = persistent && schema != null
                    && java.util.Arrays.stream(path.split("\\.")).noneMatch(PROTECTED::contains)
                    && (capability.isBoolean() && capability.asBoolean()
                    || capability.isString() && "true".equals(capability.asText()));
            // Whole object/array writes need a dedicated descendant policy; fail closed for now.
            if (schema != null && (types(schema).contains("object") || types(schema).contains("array"))) {
                writable = false;
            }
            if (fields.putIfAbsent(path, new Field(schema, persistent, writable)) != null) {
                throw new IllegalArgumentException("GROOVY_FORM_FIELD_DUPLICATE");
            }
            if (schema != null) {
                properties.set(key, schema);
            }
        }
        return properties;
    }

    private static void merge(ObjectNode target, ObjectNode source) {
        for (String name : source.propertyNames()) {
            if (target.has(name)) {
                throw new IllegalArgumentException("GROOVY_FORM_FIELD_DUPLICATE");
            }
            target.set(name, source.get(name));
        }
    }

    private static ObjectNode type(String name, boolean nullable) {
        ObjectNode schema = JSON.createObjectNode();
        if (nullable) {
            schema.putArray("type").add(name).add("null");
        } else {
            schema.put("type", name);
        }
        return schema;
    }

    private static Set<String> types(JsonNode schema) {
        JsonNode value = schema.path("type");
        Set<String> types = new HashSet<>();
        if (value.isString()) {
            types.add(value.asText());
        } else {
            value.forEach(item -> types.add(item.asText()));
        }
        return types;
    }

    private static void compatible(JsonNode source, JsonNode target) {
        Set<String> accepted = types(target);
        for (String sourceType : types(source)) {
            if (!accepted.contains(sourceType) && !("integer".equals(sourceType) && accepted.contains("number"))) {
                throw new IllegalArgumentException("GROOVY_FORM_TYPE_MISMATCH");
            }
        }
        if (types(source).contains("object")) {
            for (String name : source.path("properties").propertyNames()) {
                if (!target.path("properties").has(name)) {
                    throw new IllegalArgumentException("GROOVY_FORM_TYPE_MISMATCH");
                }
                compatible(source.path("properties").get(name), target.path("properties").get(name));
            }
            for (JsonNode required : target.path("required")) {
                if (!source.path("properties").has(required.asText())) {
                    throw new IllegalArgumentException("GROOVY_FORM_TYPE_MISMATCH");
                }
            }
        }
        if (types(source).contains("array")) {
            compatible(source.get("items"), target.get("items"));
        }
    }

    private record Field(JsonNode schema, boolean persistent, boolean writable) {
    }
}
