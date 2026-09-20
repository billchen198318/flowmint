package org.qifu.fm.domain.workflow;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.qifu.base.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Compiles and executes trusted IT-managed Groovy scripts in the FlowMint JVM. */
@Component
public class FmGroovyPreviewRunner {
    private final boolean enabled;
    private final String image;
    private final String jdkVendor;
    private final String jdkRuntimeVersion;
    private final FmGroovyProfileRevocationPolicy revocations;
    private final FmGroovyInProcessEngine engine;

    @Autowired
    public FmGroovyPreviewRunner(
            @Value("${flowmint.groovy.preview.enabled:false}") boolean enabled,
            @Value("${flowmint.groovy.preview.engine-id:jvm:groovy-5.0.6-trusted1}") String image,
            @Value("${flowmint.groovy.preview.jdk-vendor:}") String jdkVendor,
            @Value("${flowmint.groovy.preview.jdk-runtime-version:}") String jdkRuntimeVersion,
            FmGroovyProfileRevocationPolicy revocations) {
        this(enabled, image, jdkVendor, jdkRuntimeVersion, revocations, new FmGroovyInProcessEngine());
    }

    FmGroovyPreviewRunner(boolean enabled, String image, String jdkVendor, String jdkRuntimeVersion,
            FmGroovyProfileRevocationPolicy revocations, FmGroovyInProcessEngine engine) {
        this.enabled = enabled;
        this.image = image == null || image.isBlank() ? "jvm:groovy-5.0.6-trusted1" : image;
        this.jdkVendor = jdkVendor == null || jdkVendor.isBlank() ? System.getProperty("java.vendor") : jdkVendor;
        this.jdkRuntimeVersion = jdkRuntimeVersion == null || jdkRuntimeVersion.isBlank()
                ? System.getProperty("java.runtime.version") : jdkRuntimeVersion;
        this.revocations = revocations;
        this.engine = engine;
    }

    /** Compatibility constructor retained while callers migrate off worker settings. */
    public FmGroovyPreviewRunner(boolean enabled, boolean ignoredIsolation, String ignoredJava,
            String ignoredClasspath, String image, String jdkVendor, String jdkRuntimeVersion) {
        this(enabled, image, jdkVendor, jdkRuntimeVersion, new FmGroovyProfileRevocationPolicy(""),
                new FmGroovyInProcessEngine());
    }

    public FmGroovyPreviewRunner(boolean enabled, boolean ignoredIsolation, String ignoredJava,
            String ignoredClasspath, String image, String jdkVendor, String jdkRuntimeVersion,
            FmGroovyProfileRevocationPolicy revocations) {
        this(enabled, image, jdkVendor, jdkRuntimeVersion, revocations, new FmGroovyInProcessEngine());
    }

    public FmGroovyVersionManifest.EngineProfile profile() throws ServiceException {
        var profile = configuredProfile();
        revocations.requireAllowed(profile);
        return profile;
    }

    FmGroovyVersionManifest.EngineProfile configuredProfile() throws ServiceException {
        if (!enabled) {
            throw new ServiceException("GROOVY_PREVIEW_UNAVAILABLE");
        }
        return new FmGroovyVersionManifest.EngineProfile("5.0.6", jdkVendor, jdkRuntimeVersion, image, "1");
    }

    public byte[] execute(byte[] request, int timeoutMs) throws ServiceException {
        profile();
        if (request == null || request.length > 768 * 1024 || timeoutMs < 100 || timeoutMs > 10000) {
            throw new ServiceException("GROOVY_PREVIEW_REQUEST_INVALID");
        }
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            byte[] response = executor.submit(() -> engine.execute(request)).get(timeoutMs, TimeUnit.MILLISECONDS);
            profile();
            return response;
        } catch (InterruptedException cancelled) {
            Thread.currentThread().interrupt();
            throw new ServiceException("GROOVY_PREVIEW_CANCELLED");
        } catch (java.util.concurrent.TimeoutException timeout) {
            throw new ServiceException("GROOVY_TIMEOUT");
        } catch (Exception failed) {
            throw new ServiceException("GROOVY_PREVIEW_RUNNER_FAILED");
        } finally {
            executor.shutdownNow();
        }
    }
}
