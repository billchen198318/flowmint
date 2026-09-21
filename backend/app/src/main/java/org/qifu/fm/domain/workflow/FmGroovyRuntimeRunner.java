package org.qifu.fm.domain.workflow;

import org.qifu.base.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Independent Runtime enablement and admission using the in-process Groovy engine. */
@Component
public class FmGroovyRuntimeRunner {

    private final FmGroovyPreviewRunner transport;
    private final FmGroovyPreviewQuota quota;

    @Autowired
    public FmGroovyRuntimeRunner(
            @Value("${flowmint.groovy.runtime.enabled:false}") boolean enabled,
            @Value("${flowmint.groovy.runtime.engine-id:jvm:groovy-5.0.6-trusted1}") String image,
            @Value("${flowmint.groovy.runtime.jdk-vendor:}") String jdkVendor,
            @Value("${flowmint.groovy.runtime.jdk-runtime-version:}") String jdkRuntimeVersion,
            FmGroovyProfileRevocationPolicy revocations) {
        transport = new FmGroovyPreviewRunner(enabled, image, jdkVendor, jdkRuntimeVersion,
                revocations, new FmGroovyInProcessEngine());
        quota = new FmGroovyPreviewQuota();
    }

    public FmGroovyRuntimeRunner(boolean enabled, boolean isolationVerified, String javaExecutable,
            String supervisorClasspath, String image, String jdkVendor, String jdkRuntimeVersion) {
        this(enabled, isolationVerified, javaExecutable, supervisorClasspath, image, jdkVendor,
                jdkRuntimeVersion, new FmGroovyProfileRevocationPolicy(""));
    }

    /** Compatibility constructor; Docker verification no longer gates execution. */
    public FmGroovyRuntimeRunner(boolean enabled, boolean isolationVerified, String javaExecutable,
            String supervisorClasspath, String image, String jdkVendor, String jdkRuntimeVersion,
            FmGroovyProfileRevocationPolicy revocations) {
        transport = new FmGroovyPreviewRunner(enabled, image, jdkVendor, jdkRuntimeVersion,
                revocations, new FmGroovyInProcessEngine());
        quota = new FmGroovyPreviewQuota();
    }

    FmGroovyRuntimeRunner(FmGroovyPreviewRunner transport, FmGroovyPreviewQuota quota) {
        this.transport = transport;
        this.quota = quota;
    }

    public FmGroovyVersionManifest.EngineProfile profile() throws ServiceException {
        try {
            return transport.profile();
        } catch (ServiceException unavailable) {
            if ("ENGINE_PROFILE_DISABLED".equals(unavailable.getMessage())
                    || "GROOVY_PROFILE_POLICY_UNAVAILABLE".equals(unavailable.getMessage())) {
                throw unavailable;
            }
            throw new ServiceException("GROOVY_RUNTIME_UNAVAILABLE");
        }
    }

    /** Only for creating a durable rejected attempt when revocation denies admission. */
    public FmGroovyVersionManifest.EngineProfile profileForFailureRecording() throws ServiceException {
        return transport.configuredProfile();
    }

    public byte[] execute(String tenantId, FmGroovyVersionManifest.EngineProfile expectedProfile,
            byte[] request, int timeoutMs) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || expectedProfile == null
                || !expectedProfile.equals(profile())) {
            throw new ServiceException("ENGINE_PROFILE_DISABLED");
        }
        try (var permit = quota.acquire(tenantId)) {
            return transport.execute(request, timeoutMs);
        } catch (ServiceException failure) {
            // No raw supervisor messages, stderr or script logs become Runtime incidents.
            String code = switch (failure.getMessage()) {
                case "ENGINE_PROFILE_DISABLED", "GROOVY_PROFILE_POLICY_UNAVAILABLE", "GROOVY_TIMEOUT" -> failure.getMessage();
                case "GROOVY_PREVIEW_BUSY" -> "GROOVY_RUNTIME_BUSY";
                case "GROOVY_SUPERVISOR_START_FAILED" -> "GROOVY_RUNTIME_START_UNAVAILABLE";
                case null, default -> "GROOVY_RUNTIME_RUNNER_FAILED";
            };
            throw new ServiceException(code);
        }
    }
}
