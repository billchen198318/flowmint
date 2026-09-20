package org.qifu.fm.domain.workflow;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Trusted deployment file, re-read at every boundary; never supplied by an API or a script. */
@Component
public class FmGroovyProfileRevocationPolicy {

    private static final int MAX_BYTES = 65536;
    private final Path file;

    public FmGroovyProfileRevocationPolicy(
            @Value("${flowmint.groovy.profile-revocations-file:}") String filename) {
        if (filename == null || filename.isBlank()) {
            file = null;
            return;
        }
        try {
            file = Path.of(filename);
            if (!file.isAbsolute()) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("GROOVY_PROFILE_POLICY_CONFIGURATION_INVALID");
        }
    }

    public void requireAllowed(EngineProfile profile) throws ServiceException {
        if (profile == null) {
            throw new ServiceException("ENGINE_PROFILE_DISABLED");
        }
        if (file == null) {
            return;
        }
        boolean revoked;
        try {
            // Bound bytes before decoding/parsing. Do not retain a last-good policy on read failure.
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException();
            }
            byte[] bytes;
            try (var input = Files.newInputStream(file)) {
                bytes = input.readNBytes(MAX_BYTES + 1);
            }
            if (bytes.length > MAX_BYTES) {
                throw new IllegalArgumentException();
            }
            var policy = FmGroovyContractJson.object(new String(bytes, StandardCharsets.UTF_8));
            var hashes = policy.get("revokedProfileSha256");
            if (policy.size() != 1 || hashes == null || !hashes.isArray() || hashes.size() > 500) {
                throw new IllegalArgumentException();
            }
            var values = new HashSet<String>();
            for (var hash : hashes) {
                if (!hash.isTextual() || !hash.asString().matches("[a-f0-9]{64}") || !values.add(hash.asString())) {
                    throw new IllegalArgumentException();
                }
            }
            revoked = values.contains(FmGroovyContractJson.sha256(profile.canonical()));
        } catch (Exception invalid) {
            // Do not leak host paths, policy text, or filesystem exceptions into API/Incident output.
            throw new ServiceException("GROOVY_PROFILE_POLICY_UNAVAILABLE");
        }
        if (revoked) {
            throw new ServiceException("ENGINE_PROFILE_DISABLED");
        }
    }
}
