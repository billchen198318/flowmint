package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmGroovyRuntimeRunnerTest {

    @Test
    void requiresIndependentRuntimeEnablement() {
        var disabled = new FmGroovyRuntimeRunner(false, false, "", "", "", "", "");
        assertEquals("GROOVY_RUNTIME_UNAVAILABLE",
                assertThrows(ServiceException.class, disabled::profile).getMessage());
        var enabled = new FmGroovyRuntimeRunner(true, false, "", "", "", "", "");
        assertEquals("5.0.6", enabled.profile().groovyVersion());
    }

    @Test
    void onlyTrustedAdmissionAndPreLaunchFailureAreClassifiedAsTransient() throws Exception {
        var transport = mock(FmGroovyPreviewRunner.class);
        var profile = new FmGroovyVersionManifest.EngineProfile("5.0.6", "ExampleJDK", "21.0.10+7",
                "local/worker@sha256:" + "a".repeat(64), "1");
        when(transport.profile()).thenReturn(profile);
        var runner = new FmGroovyRuntimeRunner(transport, new FmGroovyPreviewQuota());
        for (String code : new String[] { "GROOVY_SUPERVISOR_START_FAILED", "GROOVY_PREVIEW_RUNNER_FAILED",
                "GROOVY_PREVIEW_CANCELLED", "GROOVY_TIMEOUT", "secret path",
                "ENGINE_PROFILE_DISABLED", "GROOVY_PROFILE_POLICY_UNAVAILABLE" }) {
            doThrow(new ServiceException(code)).when(transport).execute(any(), anyInt());
            String expected = "GROOVY_SUPERVISOR_START_FAILED".equals(code)
                    ? "GROOVY_RUNTIME_START_UNAVAILABLE" : "GROOVY_RUNTIME_RUNNER_FAILED";
            if ("ENGINE_PROFILE_DISABLED".equals(code) || "GROOVY_PROFILE_POLICY_UNAVAILABLE".equals(code)
                    || "GROOVY_TIMEOUT".equals(code)) {
                expected = code;
            }
            assertEquals(expected, assertThrows(ServiceException.class,
                    () -> runner.execute("T", profile, new byte[0], 100)).getMessage());
        }
    }

    @Test
    void busyAdmissionNeverLaunchesSupervisor() throws Exception {
        var transport = mock(FmGroovyPreviewRunner.class);
        var quota = new FmGroovyPreviewQuota();
        var profile = new FmGroovyVersionManifest.EngineProfile("5.0.6", "ExampleJDK", "21.0.10+7",
                "local/worker@sha256:" + "a".repeat(64), "1");
        when(transport.profile()).thenReturn(profile);
        var runner = new FmGroovyRuntimeRunner(transport, quota);
        try (var first = quota.acquire("T"); var second = quota.acquire("T")) {
            assertEquals("GROOVY_RUNTIME_BUSY", assertThrows(ServiceException.class,
                    () -> runner.execute("T", profile, new byte[0], 100)).getMessage());
        }
        org.mockito.Mockito.verify(transport, org.mockito.Mockito.never()).execute(any(), anyInt());
    }
}
