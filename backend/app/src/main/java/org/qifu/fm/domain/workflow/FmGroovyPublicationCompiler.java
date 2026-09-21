package org.qifu.fm.domain.workflow;

import java.util.List;
import java.util.UUID;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

/** Server-side isolated compilation; a designer's Preview result is never accepted as evidence. */
@Component
public class FmGroovyPublicationCompiler {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final FmGroovyPreviewRunner runner;
    private final FmGroovyPreviewQuota quota;

    public FmGroovyPublicationCompiler(FmGroovyPreviewRunner runner, FmGroovyPreviewQuota quota) {
        this.runner = runner;
        this.quota = quota;
    }

    public VerifiedPublication compile(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> commands, String formSchema) throws ServiceException {
        if (version == null || !"DRAFT".equals(version.getVersionStatus())) {
            throw new ServiceException("GROOVY_PUBLICATION_DRAFT_REQUIRED");
        }
        var profile = runner.profile();
        var manifest = FmGroovyVersionManifest.build(version, processKey, commands, profile);
        var bindings = new FmGroovyDraftValidator().validate(version, version.getBpmnXml(), commands);
        try (var permit = quota.acquire(version.getTenantId())) {
            var form = new FmGroovyFormContract(formSchema);
            // Reject invalid mappings for the whole version before invoking any worker.
            for (var binding : bindings) {
                form.validate(binding);
            }
            for (var binding : bindings) {
                var request = JSON.createObjectNode();
                request.put("protocolVersion", 2);
                request.put("profile", FmGroovyDraftValidator.PROFILE);
                request.put("invocationId", UUID.randomUUID().toString());
                request.put("attemptId", UUID.randomUUID().toString());
                request.put("generation", 1);
                request.put("bindingSha256", manifest.bindings().stream()
                        .filter(digest -> digest.bindingId().equals(binding.getBindingId()))
                        .findFirst().orElseThrow().sha256());
                request.put("operation", "CHECK");
                request.put("script", binding.getScriptContent());
                request.putObject("input");
                request.putObject("context");
                request.set("inputSchema", FmGroovyContractJson.object(binding.getInputSchema()));
                request.set("outputSchema", FmGroovyContractJson.object(binding.getOutputSchema()));
                byte[] response = runner.execute(JSON.writeValueAsBytes(request), binding.getTimeoutMs());
                var checked = FmGroovyPreviewProtocol.response(response, request, manifest.sha256(), null, 0);
                if (!"VALID".equals(checked.status())) {
                    throw new ServiceException("GROOVY_PUBLICATION_COMPILE_FAILED");
                }
            }
            if (!profile.equals(runner.profile())) {
                throw new ServiceException("GROOVY_PUBLICATION_PROFILE_CHANGED");
            }
            return new VerifiedPublication(manifest, profile, FmGroovyContractJson.sha256(formSchema));
        } catch (IllegalArgumentException invalid) {
            throw new ServiceException("GROOVY_PUBLICATION_CONTRACT_INVALID");
        }
    }

    /** Only this compiler can construct evidence; it is not a request DTO or a publication authorization. */
    public static final class VerifiedPublication {

        private final FmGroovyVersionManifest.Manifest manifest;
        private final FmGroovyVersionManifest.EngineProfile profile;
        private final String formSchemaSha256;

        private VerifiedPublication(FmGroovyVersionManifest.Manifest manifest,
                FmGroovyVersionManifest.EngineProfile profile, String formSchemaSha256) {
            this.manifest = manifest;
            this.profile = profile;
            this.formSchemaSha256 = formSchemaSha256;
        }

        public FmGroovyVersionManifest.Manifest manifest() {
            return manifest;
        }

        public FmGroovyVersionManifest.EngineProfile profile() {
            return profile;
        }

        public String formSchemaSha256() {
            return formSchemaSha256;
        }
    }
}
