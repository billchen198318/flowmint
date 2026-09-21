package org.qifu.fm.domain.workflow;

import java.util.Comparator;
import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessSystemTask;
import org.qifu.fm.entity.FmProcessVersion;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** Content identity for the future publisher. Producing a hash does not authorize publication. */
public final class FmGroovyVersionManifest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private FmGroovyVersionManifest() {
    }

    /** Verify stored publication content against the trusted version and pinned deployment profile. */
    public static Manifest verify(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> commands, EngineProfile profile,
            String storedManifest, String storedSha256) throws ServiceException {
        if (version == null || !("PUBLISHED".equals(version.getVersionStatus())
                || "RETIRED".equals(version.getVersionStatus()))) {
            throw new IllegalArgumentException("GROOVY_VERSION_NOT_PUBLISHED");
        }
        Manifest actual = build(version, processKey, commands, profile);
        if (!actual.canonicalJson().equals(storedManifest) || !actual.sha256().equals(storedSha256)
                || !actual.bpmnSha256().equals(version.getBpmnSha256())) {
            throw new IllegalArgumentException("GROOVY_MANIFEST_MISMATCH");
        }
        return actual;
    }

    public static Manifest build(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> commands, EngineProfile profile) throws ServiceException {
        if (version == null || version.getTenantId() == null || version.getTenantId().isBlank()
                || version.getProcessDefId() == null || version.getProcessDefId().isBlank()
                || version.getVersionNo() == null || version.getVersionNo() < 1 || profile == null) {
            throw new IllegalArgumentException("GROOVY_MANIFEST_IDENTITY_INVALID");
        }
        String xml = version.getBpmnXml();
        new FmBpmnDesignValidator().validateDraft(xml, processKey, true);
        List<FmProcessSystemTask> bindings = new FmGroovyDraftValidator().validate(version, xml, commands);
        if (bindings.isEmpty()) {
            throw new IllegalArgumentException("GROOVY_MANIFEST_BINDINGS_REQUIRED");
        }
        String profileJson = profile.canonical();
        List<BindingDigest> digests = bindings.stream()
                .sorted(Comparator.comparing(FmProcessSystemTask::getBindingId))
                .map(binding -> new BindingDigest(binding.getNodeId(), binding.getBindingId(),
                        FmGroovyContractJson.bindingHash(binding.getScriptContent(),
                                FmGroovyContractJson.object(binding.getInputSchema()),
                                FmGroovyContractJson.object(binding.getOutputSchema()),
                                FmGroovyContractJson.object(binding.getMappingContent()),
                                binding.getTimeoutMs(), profileJson, profile.policyVersion())))
                .toList();
        String bpmnSha256 = FmGroovyContractJson.sha256(xml);
        ObjectNode manifest = JSON.createObjectNode();
        manifest.put("manifestVersion", 1);
        manifest.put("tenantId", version.getTenantId());
        manifest.put("processDefId", version.getProcessDefId());
        manifest.put("processKey", processKey);
        manifest.put("versionNo", version.getVersionNo());
        manifest.put("bpmnSha256", bpmnSha256);
        manifest.set("engineProfile", FmGroovyContractJson.object(profileJson));
        ArrayNode entries = manifest.putArray("bindings");
        for (BindingDigest digest : digests) {
            ObjectNode entry = entries.addObject();
            entry.put("nodeId", digest.nodeId());
            entry.put("bindingId", digest.bindingId());
            entry.put("sha256", digest.sha256());
        }
        String canonical = FmGroovyContractJson.canonical(manifest);
        return new Manifest(canonical, FmGroovyContractJson.sha256(canonical), bpmnSha256, digests);
    }

    /** Supplied by trusted deployment configuration, never inferred from a designer's script. */
    public record EngineProfile(String groovyVersion, String jdkVendor, String jdkRuntimeVersion,
            String workerImage, String policyVersion) {

        public EngineProfile {
            if (!"5.0.6".equals(groovyVersion) || !"1".equals(policyVersion)
                    || jdkVendor == null || !jdkVendor.matches("[A-Za-z0-9][A-Za-z0-9 ._-]{0,99}")
                    || jdkRuntimeVersion == null
                    || !jdkRuntimeVersion.matches("21(?:\\.[0-9]+)*\\+[0-9]+(?:[-.][A-Za-z0-9]+)*")
                    || jdkRuntimeVersion.length() > 100
                    || workerImage == null || workerImage.length() > 300
                    || !(workerImage.matches("jvm:[A-Za-z0-9][A-Za-z0-9._:-]{0,199}")
                            || workerImage.matches("[a-zA-Z0-9][a-zA-Z0-9._/:-]*@sha256:[a-f0-9]{64}"))) {
                throw new IllegalArgumentException("GROOVY_ENGINE_PROFILE_INVALID");
            }
        }

        public String canonical() {
            ObjectNode value = JSON.createObjectNode();
            value.put("groovyVersion", groovyVersion);
            value.put("jdkVendor", jdkVendor);
            value.put("jdkRuntimeVersion", jdkRuntimeVersion);
            value.put("workerImage", workerImage);
            value.put("policyVersion", policyVersion);
            return FmGroovyContractJson.canonical(value);
        }
    }

    public record BindingDigest(String nodeId, String bindingId, String sha256) {
    }

    public record Manifest(String canonicalJson, String sha256, String bpmnSha256, List<BindingDigest> bindings) {

        public Manifest {
            bindings = List.copyOf(bindings);
        }
    }
}
