package org.qifu.fm.domain.workflow;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.entity.FmProcessSystemTask;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** Pure preparation for Runtime transactions; never invokes a worker or writes a database. */
public final class FmGroovyRuntimeMapping {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final JsonNode inputSchema;
    private final JsonNode outputSchema;
    private final JsonNode mapping;
    private final String tenantId;
    private final String processDefId;
    private final Integer versionNo;
    private final String nodeId;

    public FmGroovyRuntimeMapping(FmProcessSystemTask binding) {
        if (binding == null || !"GROOVY".equals(binding.getTaskType())) {
            throw new IllegalArgumentException("GROOVY_BINDING_INVALID");
        }
        inputSchema = FmGroovyContractJson.object(binding.getInputSchema());
        outputSchema = FmGroovyContractJson.object(binding.getOutputSchema());
        mapping = FmGroovyContractJson.object(binding.getMappingContent());
        FmGroovyContractJson.validateSchema(inputSchema);
        FmGroovyContractJson.validateSchema(outputSchema);
        if (!"object".equals(inputSchema.path("type").asText())
                || !"object".equals(outputSchema.path("type").asText())) {
            throw new IllegalArgumentException("GROOVY_ROOT_SCHEMA_INVALID");
        }
        FmGroovyDraftValidator.validateMapping(mapping, inputSchema, outputSchema);
        tenantId = binding.getTenantId();
        processDefId = binding.getProcessDefId();
        versionNo = binding.getVersionNo();
        nodeId = binding.getNodeId();
    }

    public InputSnapshot input(String savedFormJson, TrustedContext context, int revisionNo) {
        if (context == null || !context.tenantId().equals(tenantId)
                || !context.processDefId().equals(processDefId)
                || !Integer.valueOf(context.processVersionNo()).equals(versionNo)
                || !context.nodeId().equals(nodeId) || revisionNo < 1) {
            throw new IllegalArgumentException("GROOVY_INPUT_IDENTITY_INVALID");
        }
        JsonNode form = FmGroovyContractJson.object(savedFormJson);
        JsonNode trusted = context.json();
        ObjectNode input = JSON.createObjectNode();
        for (String name : inputSchema.path("properties").propertyNames()) {
            JsonNode entry = mapping.path("input").get(name);
            JsonNode value = null;
            if (entry != null) {
                value = switch (entry.path("source").asText()) {
                    case "FORM_DATA" -> read(form, entry.path("path").asText());
                    case "PROCESS_CONTEXT" -> trusted.get(entry.path("path").asText());
                    case "CONSTANT" -> entry.get("value");
                    default -> throw new IllegalArgumentException("GROOVY_INPUT_SOURCE_INVALID");
                };
            }
            if (value == null || value.isMissingNode()) {
                if (contains(inputSchema.path("required"), name)) {
                    throw new IllegalArgumentException("GROOVY_INPUT_REQUIRED");
                }
                input.putNull(name);
            } else {
                input.set(name, value.deepCopy());
            }
        }
        FmGroovyContractJson.validateValue(input, inputSchema);
        String inputJson = FmGroovyContractJson.canonical(input);
        String contextJson = FmGroovyContractJson.canonical(trusted);
        // Round-trip enforces the serialized byte limit as well as depth/node limits.
        FmGroovyContractJson.object(inputJson);
        ObjectNode envelope = JSON.createObjectNode();
        envelope.put("revisionNo", revisionNo);
        envelope.set("input", input);
        envelope.set("context", trusted);
        String hash = FmGroovyContractJson.sha256(FmGroovyContractJson.canonical(envelope));
        return new InputSnapshot(inputJson, contextJson, revisionNo, hash);
    }

    public ApplyPlan apply(String savedFormJson, String resultJson,
            Set<String> writablePaths, String formSchema, FmFormSubmissionValidator validator)
            throws ServiceException {
        if (writablePaths == null || validator == null) {
            throw new IllegalArgumentException("GROOVY_WRITE_POLICY_REQUIRED");
        }
        JsonNode result = FmGroovyContractJson.object(resultJson);
        FmGroovyContractJson.validateValue(result, outputSchema);
        ObjectNode merged = (ObjectNode) FmGroovyContractJson.object(savedFormJson);
        Set<String> written = new HashSet<>();
        for (String name : mapping.path("output").propertyNames()) {
            JsonNode entry = mapping.path("output").get(name);
            if ("DISCARD".equals(entry.path("target").asText())) {
                continue;
            }
            String path = entry.path("path").asText();
            if (!writablePaths.contains(path)) {
                throw new IllegalArgumentException("GROOVY_FORM_WRITE_DENIED");
            }
            // Optional absent output leaves the saved value intact; explicit null is a write.
            if (!result.has(name)) {
                continue;
            }
            write(merged, path, result.get(name));
            written.add(path);
        }
        String mergedJson = FmGroovyContractJson.canonical(merged);
        FmGroovyContractJson.object(mergedJson);
        if (!written.isEmpty()) {
            Map<String, Object> data = JSON.convertValue(merged, new TypeReference<Map<String, Object>>() { });
            validator.validate(formSchema, data);
        }
        return new ApplyPlan(mergedJson, !written.isEmpty(), written,
                FmGroovyContractJson.sha256(FmGroovyContractJson.canonical(result)));
    }

    private static JsonNode read(JsonNode source, String path) {
        JsonNode value = source;
        for (String part : path.split("\\.")) {
            if (value == null || value.isNull()) {
                return null;
            }
            if (!value.isObject()) {
                throw new IllegalArgumentException("GROOVY_INPUT_PATH_TYPE_INVALID");
            }
            value = value.get(part);
        }
        return value;
    }

    private static void write(ObjectNode target, String path, JsonNode value) {
        String[] parts = path.split("\\.");
        ObjectNode current = target;
        for (int index = 0; index < parts.length - 1; index++) {
            JsonNode child = current.get(parts[index]);
            if (child == null || child.isNull()) {
                child = current.putObject(parts[index]);
            }
            if (!child.isObject()) {
                throw new IllegalArgumentException("GROOVY_OUTPUT_PATH_TYPE_INVALID");
            }
            current = (ObjectNode) child;
        }
        current.set(parts[parts.length - 1], value.deepCopy());
    }

    private static boolean contains(JsonNode values, String value) {
        for (JsonNode item : values) {
            if (value.equals(item.asText())) {
                return true;
            }
        }
        return false;
    }

    public record InputSnapshot(String inputJson, String contextJson, int revisionNo, String sha256) {
    }

    public record ApplyPlan(String formJson, boolean requiresWrite, Set<String> writtenPaths, String resultSha256) {
        public ApplyPlan {
            writtenPaths = Set.copyOf(writtenPaths);
        }
    }

    /** Construct only from persisted process identity; no browser or form fields are trusted here. */
    public record TrustedContext(String tenantId, String processDefId, int processVersionNo,
            String processInstanceId, String nodeId, String invocationId, Instant startedAt,
            String businessKey, String documentNo, String applicantAccount, String initiatorAccount,
            String applicantOrgUnitId) {
        public TrustedContext {
            for (String required : new String[] { tenantId, processDefId, processInstanceId, nodeId,
                    invocationId, businessKey, applicantAccount, initiatorAccount }) {
                if (required == null || required.isBlank() || required.length() > 200) {
                    throw new IllegalArgumentException("GROOVY_CONTEXT_INVALID");
                }
            }
            if (processVersionNo < 1 || startedAt == null) {
                throw new IllegalArgumentException("GROOVY_CONTEXT_INVALID");
            }
        }

        private JsonNode json() {
            ObjectNode value = JSON.createObjectNode();
            value.put("tenantId", tenantId);
            value.put("processInstanceId", processInstanceId);
            value.put("processVersionNo", processVersionNo);
            value.put("nodeId", nodeId);
            value.put("invocationId", invocationId);
            value.put("startedAt", startedAt.toString());
            value.put("mode", "RUNTIME");
            value.put("businessKey", businessKey);
            value.put("documentNo", documentNo);
            value.put("applicantAccount", applicantAccount);
            value.put("initiatorAccount", initiatorAccount);
            value.put("applicantOrgUnitId", applicantOrgUnitId);
            return value;
        }
    }
}
