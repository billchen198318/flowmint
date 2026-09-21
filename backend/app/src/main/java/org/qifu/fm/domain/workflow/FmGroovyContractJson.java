package org.qifu.fm.domain.workflow;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Bounded contract parsing and deterministic hashing; never loads a Groovy compiler. */
public final class FmGroovyContractJson {

    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    private static final Set<String> KEYWORDS = Set.of(
            "type", "properties", "required", "items", "enum", "additionalProperties",
            "minLength", "maxLength", "minItems", "maxItems", "minimum", "maximum");
    private static final Set<String> TYPES = Set.of(
            "object", "array", "string", "number", "integer", "boolean", "null");

    private FmGroovyContractJson() {
    }

    public static String normalizeScript(String script) {
        if (script == null) {
            throw new IllegalArgumentException("GROOVY_SCRIPT_REQUIRED");
        }
        validUnicode(script);
        String normalized = script.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank() || normalized.getBytes(StandardCharsets.UTF_8).length > 65536) {
            throw new IllegalArgumentException("GROOVY_SCRIPT_SIZE_INVALID");
        }
        return normalized;
    }

    public static JsonNode object(String content) {
        if (content == null || content.getBytes(StandardCharsets.UTF_8).length > 262144) {
            throw new IllegalArgumentException("GROOVY_JSON_SIZE_INVALID");
        }
        JsonNode value;
        try {
            value = JSON.readTree(content);
        } catch (Exception invalid) {
            // Parser diagnostics may contain source text or constants supplied by a designer.
            throw new IllegalArgumentException("GROOVY_JSON_INVALID");
        }
        if (value == null || !value.isObject()) {
            throw new IllegalArgumentException("GROOVY_JSON_OBJECT_REQUIRED");
        }
        bounded(value, 0, new int[] { 0 });
        return value;
    }

    public static String canonical(JsonNode value) {
        bounded(value, 0, new int[] { 0 });
        return JSON.writeValueAsString(ordered(value));
    }

    public static String sha256(String content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA256_UNAVAILABLE", unavailable);
        }
    }

    public static String bindingHash(String script, JsonNode input, JsonNode output, JsonNode mapping,
            int timeoutMs, String engineProfile, String policyVersion) {
        String normalizedScript = normalizeScript(script);
        validateSchema(input);
        validateSchema(output);
        bounded(mapping, 0, new int[] { 0 });
        if (!mapping.isObject() || timeoutMs < 100 || timeoutMs > 10000
                || engineProfile == null || engineProfile.isBlank()
                || policyVersion == null || policyVersion.isBlank()) {
            throw new IllegalArgumentException("GROOVY_HASH_CONTRACT_INVALID");
        }
        validUnicode(engineProfile);
        validUnicode(policyVersion);
        ObjectNode value = JSON.createObjectNode();
        value.put("hashVersion", 1);
        value.put("script", normalizedScript);
        value.set("inputSchema", input);
        value.set("outputSchema", output);
        value.set("mapping", mapping);
        value.put("timeoutMs", timeoutMs);
        value.put("engineProfile", engineProfile);
        value.put("policyVersion", policyVersion);
        // Each contract is individually bounded. The envelope adds a level and combines them.
        return sha256(JSON.writeValueAsString(ordered(value)));
    }

    public static void validateSchema(JsonNode schema) {
        bounded(schema, 0, new int[] { 0 });
        schema(schema);
    }

    public static void validateValue(JsonNode value, JsonNode schema) {
        bounded(value, 0, new int[] { 0 });
        validateSchema(schema);
        value(value, schema);
    }

    private static void value(JsonNode value, JsonNode schema) {
        JsonNode types = schema.path("type");
        boolean matches = types.isString() && matches(value, types.asText());
        if (types.isArray()) {
            for (JsonNode type : types) {
                matches |= matches(value, type.asText());
            }
        }
        if (!matches) {
            throw new IllegalArgumentException("VALUE_TYPE_INVALID");
        }
        if (schema.has("enum")) {
            boolean found = false;
            String normalized = canonical(value);
            for (JsonNode candidate : schema.get("enum")) {
                found |= normalized.equals(canonical(candidate));
            }
            if (!found) {
                throw new IllegalArgumentException("VALUE_ENUM_INVALID");
            }
        }
        if (value.isObject()) {
            JsonNode properties = schema.path("properties");
            for (JsonNode required : schema.path("required")) {
                if (!value.has(required.asText())) {
                    throw new IllegalArgumentException("VALUE_REQUIRED");
                }
            }
            for (String name : value.propertyNames()) {
                if (!properties.has(name)) {
                    throw new IllegalArgumentException("VALUE_UNKNOWN_FIELD");
                }
                value(value.get(name), properties.get(name));
            }
        } else if (value.isArray()) {
            bounds(BigDecimal.valueOf(value.size()), schema, "minItems", "maxItems");
            for (JsonNode item : value) {
                value(item, schema.get("items"));
            }
        } else if (value.isString()) {
            String text = value.asText();
            bounds(BigDecimal.valueOf(text.codePointCount(0, text.length())),
                    schema, "minLength", "maxLength");
        } else if (value.isNumber()) {
            bounds(value.decimalValue(), schema, "minimum", "maximum");
        }
    }

    private static boolean matches(JsonNode value, String type) {
        return switch (type) {
            case "object" -> value.isObject();
            case "array" -> value.isArray();
            case "string" -> value.isString();
            case "boolean" -> value.isBoolean();
            case "null" -> value.isNull();
            case "number" -> value.isNumber();
            case "integer" -> value.isNumber() && value.decimalValue().stripTrailingZeros().scale() <= 0;
            default -> false;
        };
    }

    private static void bounds(BigDecimal value, JsonNode schema, String minimum, String maximum) {
        if (schema.has(minimum) && value.compareTo(schema.get(minimum).decimalValue()) < 0
                || schema.has(maximum) && value.compareTo(schema.get(maximum).decimalValue()) > 0) {
            throw new IllegalArgumentException("VALUE_BOUND_INVALID");
        }
    }

    private static void schema(JsonNode schema) {
        if (!schema.isObject() || !KEYWORDS.containsAll(schema.propertyNames())) {
            throw new IllegalArgumentException("SCHEMA_INVALID");
        }
        JsonNode type = schema.path("type");
        Set<String> types = new HashSet<>();
        if (type.isString()) {
            types.add(type.asText());
        } else if (type.isArray() && !type.isEmpty()) {
            for (JsonNode name : type) {
                if (!name.isString() || !types.add(name.asText())) {
                    throw new IllegalArgumentException("SCHEMA_TYPE_INVALID");
                }
            }
        }
        if (types.isEmpty() || !TYPES.containsAll(types)) {
            throw new IllegalArgumentException("SCHEMA_TYPE_INVALID");
        }
        if (schema.has("additionalProperties") && (!schema.get("additionalProperties").isBoolean()
                || schema.get("additionalProperties").asBoolean())) {
            throw new IllegalArgumentException("SCHEMA_ADDITIONAL_PROPERTIES_INVALID");
        }
        JsonNode properties = schema.path("properties");
        if (schema.has("properties")) {
            if (!properties.isObject()) {
                throw new IllegalArgumentException("SCHEMA_PROPERTIES_INVALID");
            }
            for (JsonNode child : properties) {
                schema(child);
            }
        }
        if (types.contains("array") && !schema.has("items")) {
            throw new IllegalArgumentException("SCHEMA_ITEMS_REQUIRED");
        }
        if (schema.has("items")) {
            schema(schema.get("items"));
        }
        if (schema.has("required")) {
            JsonNode required = schema.get("required");
            Set<String> seen = new HashSet<>();
            if (!required.isArray()) {
                throw new IllegalArgumentException("SCHEMA_REQUIRED_INVALID");
            }
            for (JsonNode name : required) {
                if (!name.isString() || !properties.has(name.asText()) || !seen.add(name.asText())) {
                    throw new IllegalArgumentException("SCHEMA_REQUIRED_INVALID");
                }
            }
        }
        if (schema.has("enum") && (!schema.get("enum").isArray() || schema.get("enum").isEmpty())) {
            throw new IllegalArgumentException("SCHEMA_ENUM_INVALID");
        }
        for (String key : List.of("minimum", "maximum", "minLength", "maxLength", "minItems", "maxItems")) {
            if (!schema.has(key)) {
                continue;
            }
            JsonNode bound = schema.get(key);
            boolean count = !Set.of("minimum", "maximum").contains(key);
            if (!bound.isNumber() || count && (bound.decimalValue().signum() < 0
                    || bound.decimalValue().stripTrailingZeros().scale() > 0)) {
                throw new IllegalArgumentException("SCHEMA_BOUND_INVALID");
            }
        }
        for (List<String> pair : List.of(List.of("minimum", "maximum"),
                List.of("minLength", "maxLength"), List.of("minItems", "maxItems"))) {
            if (schema.has(pair.get(0)) && schema.has(pair.get(1))
                    && schema.get(pair.get(0)).decimalValue().compareTo(schema.get(pair.get(1)).decimalValue()) > 0) {
                throw new IllegalArgumentException("SCHEMA_BOUND_INVALID");
            }
        }
    }

    private static JsonNode ordered(JsonNode value) {
        if (value.isObject()) {
            ObjectNode object = JSON.createObjectNode();
            List<String> names = new ArrayList<>(value.propertyNames());
            names.sort(String::compareTo);
            for (String name : names) {
                object.set(name, ordered(value.get(name)));
            }
            return object;
        }
        if (value.isArray()) {
            ArrayNode array = JSON.createArrayNode();
            for (JsonNode item : value) {
                array.add(ordered(item));
            }
            return array;
        }
        if (value.isNumber()) {
            return JSON.getNodeFactory().numberNode(value.decimalValue().stripTrailingZeros());
        }
        return value;
    }

    private static void bounded(JsonNode value, int depth, int[] count) {
        if (value == null || depth > 20 || ++count[0] > 10000) {
            throw new IllegalArgumentException("GROOVY_JSON_LIMIT");
        }
        if (value.isObject() || value.isArray()) {
            if (value.isObject()) {
                value.propertyNames().forEach(FmGroovyContractJson::validUnicode);
            }
            for (JsonNode child : value) {
                bounded(child, depth + 1, count);
            }
        } else if (value.isString()) {
            validUnicode(value.asText());
        }
    }

    private static void validUnicode(String text) {
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (Character.isHighSurrogate(character)) {
                if (++index >= text.length() || !Character.isLowSurrogate(text.charAt(index))) {
                    throw new IllegalArgumentException("GROOVY_UNICODE_INVALID");
                }
            } else if (Character.isLowSurrogate(character)) {
                throw new IllegalArgumentException("GROOVY_UNICODE_INVALID");
            }
        }
    }
}
