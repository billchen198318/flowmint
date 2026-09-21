package org.qifu.fm.domain.workflow;

import java.util.List;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Prepares a verified node from persisted version and form content.
 * The caller must load the pinned form version and enforce authorization, profile
 * availability and transaction fencing. This class never runs scripts or writes data.
 */
public final class FmGroovyRuntimePreparation {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final FmGroovyRuntimeMapping mapping;
    private final Set<String> writablePaths;
    private final String formSchema;
    private final String manifestSha256;
    private final String bindingSha256;
    private final String bindingId;
    private final String script;
    private final String inputSchema;
    private final String outputSchema;
    private final int timeoutMs;
    private final String tenantId;
    private final String nodeId;
    private final int versionNo;

    public FmGroovyRuntimePreparation(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> bindings, FmGroovyVersionManifest.EngineProfile profile,
            String storedManifest, String storedSha256, String nodeId, String formSchema)
            throws ServiceException {
        var manifest = FmGroovyVersionManifest.verify(version, processKey, bindings, profile,
                storedManifest, storedSha256);
        var binding = new FmGroovyDraftValidator().validate(version, version.getBpmnXml(), bindings)
                .stream().filter(candidate -> candidate.getNodeId().equals(nodeId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("GROOVY_NODE_NOT_FOUND"));
        writablePaths = new FmGroovyFormContract(formSchema).validate(binding);
        mapping = new FmGroovyRuntimeMapping(binding);
        this.formSchema = formSchema;
        manifestSha256 = manifest.sha256();
        bindingSha256 = manifest.bindings().stream()
                .filter(candidate -> candidate.nodeId().equals(nodeId)).findFirst().orElseThrow().sha256();
        bindingId = binding.getBindingId();
        script = binding.getScriptContent();
        inputSchema = binding.getInputSchema();
        outputSchema = binding.getOutputSchema();
        timeoutMs = binding.getTimeoutMs();
        tenantId = version.getTenantId();
        this.nodeId = nodeId;
        versionNo = version.getVersionNo();
    }

    public FmGroovyRuntimeMapping.InputSnapshot input(String savedFormJson,
            FmGroovyRuntimeMapping.TrustedContext context, int revisionNo) {
        return mapping.input(savedFormJson, context, revisionNo);
    }

    /** Result protocol and lease/generation must be verified by the caller before applying. */
    public FmGroovyRuntimeMapping.ApplyPlan apply(String savedFormJson, String resultJson,
            FmFormSubmissionValidator validator) throws ServiceException {
        return mapping.apply(savedFormJson, resultJson, writablePaths, formSchema, validator);
    }

    public String manifestSha256() {
        return manifestSha256;
    }

    public String bindingSha256() {
        return bindingSha256;
    }

    public String bindingId() {
        return bindingId;
    }

    public int timeoutMs() {
        return timeoutMs;
    }

    /** Internal wire request, using the publication-verified script and a persisted input snapshot. */
    public ObjectNode runRequest(FmGroovyRuntimeMapping.InputSnapshot snapshot, String attemptId, int generation) {
        if (snapshot == null || attemptId == null || !attemptId.matches("[A-Za-z0-9_-]{1,64}")
                || generation < 1 || snapshot.revisionNo() < 1) {
            throw new IllegalArgumentException("GROOVY_RUNTIME_REQUEST_INVALID");
        }
        var input = FmGroovyContractJson.object(snapshot.inputJson());
        var context = FmGroovyContractJson.object(snapshot.contextJson());
        var schema = FmGroovyContractJson.object(inputSchema);
        FmGroovyContractJson.validateValue(input, schema);
        ObjectNode envelope = JSON.createObjectNode();
        envelope.put("revisionNo", snapshot.revisionNo());
        envelope.set("input", input);
        envelope.set("context", context);
        if (!"RUNTIME".equals(context.path("mode").asText())
                || !tenantId.equals(context.path("tenantId").asText())
                || !nodeId.equals(context.path("nodeId").asText())
                || !context.path("processVersionNo").isIntegralNumber()
                || context.path("processVersionNo").asLong() != versionNo
                || !context.path("invocationId").asText().matches("[A-Za-z0-9_-]{1,64}")
                || !FmGroovyContractJson.sha256(FmGroovyContractJson.canonical(envelope)).equals(snapshot.sha256())) {
            throw new IllegalArgumentException("GROOVY_RUNTIME_SNAPSHOT_INVALID");
        }
        ObjectNode request = JSON.createObjectNode();
        request.put("protocolVersion", 2);
        request.put("profile", FmGroovyDraftValidator.PROFILE);
        request.put("invocationId", context.get("invocationId").asText());
        request.put("attemptId", attemptId);
        request.put("generation", generation);
        request.put("bindingSha256", bindingSha256);
        request.put("operation", "RUN");
        request.put("script", script);
        request.set("input", input);
        request.set("context", context);
        request.set("inputSchema", schema);
        request.set("outputSchema", FmGroovyContractJson.object(outputSchema));
        return request;
    }
}
