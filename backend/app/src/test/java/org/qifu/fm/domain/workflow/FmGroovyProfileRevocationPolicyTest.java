package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;

class FmGroovyProfileRevocationPolicyTest {

    @TempDir
    Path directory;

    private final EngineProfile profile = new EngineProfile("5.0.6", "ExampleJDK", "21.0.10+7",
            "local/worker@sha256:" + "a".repeat(64), "1");

    @Test
    void readsReplacementPolicyWithoutRestartAndMatchesFullProfileHash() throws Exception {
        Path file = directory.resolve("revocations.json");
        Files.writeString(file, "{\"revokedProfileSha256\":[]}");
        var policy = new FmGroovyProfileRevocationPolicy(file.toString());
        policy.requireAllowed(profile);
        Files.writeString(file, "{\"revokedProfileSha256\":[\""
                + FmGroovyContractJson.sha256(profile.canonical()) + "\"]}");
        assertEquals("ENGINE_PROFILE_DISABLED",
                assertThrows(ServiceException.class, () -> policy.requireAllowed(profile)).getMessage());
        var different = new EngineProfile("5.0.6", "ExampleJDK", "21.0.11+1", profile.workerImage(), "1");
        policy.requireAllowed(different);
    }

    @Test
    void missingOrMalformedFileNeverUsesLastGoodPolicy() throws Exception {
        Path file = directory.resolve("revocations.json");
        Files.writeString(file, "{\"revokedProfileSha256\":[]}");
        var policy = new FmGroovyProfileRevocationPolicy(file.toString());
        policy.requireAllowed(profile);
        for (String content : new String[] { "", "{}", "{\"revokedProfileSha256\":null}",
                "{\"revokedProfileSha256\":[\"bad\"]}", "{\"revokedProfileSha256\":[],\"extra\":true}",
                "{\"revokedProfileSha256\":[],\"revokedProfileSha256\":[]}",
                "{\"revokedProfileSha256\":[]} {}", " ".repeat(65537) }) {
            Files.writeString(file, content);
            assertEquals("GROOVY_PROFILE_POLICY_UNAVAILABLE",
                    assertThrows(ServiceException.class, () -> policy.requireAllowed(profile)).getMessage());
        }
        Files.delete(file);
        assertEquals("GROOVY_PROFILE_POLICY_UNAVAILABLE",
                assertThrows(ServiceException.class, () -> policy.requireAllowed(profile)).getMessage());
    }

    @Test
    void duplicateHashesAndTooManyEntriesAreRejected() throws Exception {
        Path file = directory.resolve("revocations.json");
        String hash = "\"" + "a".repeat(64) + "\"";
        Files.writeString(file, "{\"revokedProfileSha256\":[" + hash + "," + hash + "]}");
        var policy = new FmGroovyProfileRevocationPolicy(file.toString());
        assertThrows(ServiceException.class, () -> policy.requireAllowed(profile));
        Files.writeString(file, "{\"revokedProfileSha256\":[" + (hash + ",").repeat(500) + hash + "]}");
        assertThrows(ServiceException.class, () -> policy.requireAllowed(profile));
    }

    @Test
    void optionalPolicyDoesNotEnableRunnerAndConfiguredPathMustBeAbsolute() {
        assertDoesNotThrow(() -> new FmGroovyProfileRevocationPolicy("").requireAllowed(profile));
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyProfileRevocationPolicy("relative.json"));
        var runner = new FmGroovyRuntimeRunner(false, false, "", "", "", "", "",
                new FmGroovyProfileRevocationPolicy(""));
        assertEquals("GROOVY_RUNTIME_UNAVAILABLE", assertThrows(ServiceException.class, runner::profile).getMessage());
    }

    @Test
    void previewAndRuntimeShareRevocationAndPreserveItsSafeCode() throws Exception {
        Path file = directory.resolve("revocations.json");
        Files.writeString(file, "{\"revokedProfileSha256\":[\""
                + FmGroovyContractJson.sha256(profile.canonical()) + "\"]}");
        var policy = new FmGroovyProfileRevocationPolicy(file.toString());
        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        if (!Files.isRegularFile(Path.of(java))) {
            java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        }
        var preview = new FmGroovyPreviewRunner(true, true, java, "classpath", profile.workerImage(),
                profile.jdkVendor(), profile.jdkRuntimeVersion(), policy);
        var runtime = new FmGroovyRuntimeRunner(true, true, java, "classpath", profile.workerImage(),
                profile.jdkVendor(), profile.jdkRuntimeVersion(), policy);
        assertEquals("ENGINE_PROFILE_DISABLED", assertThrows(ServiceException.class, preview::profile).getMessage());
        assertEquals("ENGINE_PROFILE_DISABLED", assertThrows(ServiceException.class, runtime::profile).getMessage());
        assertEquals("ENGINE_PROFILE_DISABLED", assertThrows(ServiceException.class,
                () -> runtime.execute("T", profile, new byte[0], 100)).getMessage());
        Files.writeString(file, "broken");
        assertEquals("GROOVY_PROFILE_POLICY_UNAVAILABLE",
                assertThrows(ServiceException.class, runtime::profile).getMessage());
    }
}
