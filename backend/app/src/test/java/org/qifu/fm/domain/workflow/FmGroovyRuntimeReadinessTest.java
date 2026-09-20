package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.flowable.job.service.impl.asyncexecutor.AsyncExecutor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

class FmGroovyRuntimeReadinessTest {

    @Test
    void rejectsMissingEngineAndInactiveExecutorBeforeCheckingRunners() {
        var compiler = mock(FmGroovyPreviewRunner.class);
        var runtime = mock(FmGroovyRuntimeRunner.class);
        var manager = mock(PlatformTransactionManager.class);
        try (var context = new StaticApplicationContext()) {
            var readiness = new FmGroovyRuntimeReadiness(compiler, runtime,
                    context.getBeanProvider(SpringProcessEngineConfiguration.class), manager);
            assertFalse(readiness.isReady());
            var engine = new SpringProcessEngineConfiguration();
            engine.setTransactionManager(manager);
            context.getBeanFactory().registerSingleton("engine", engine);
            assertFalse(readiness.isReady());
            engine.setAsyncExecutor(mock(AsyncExecutor.class));
            assertFalse(readiness.isReady());
            verifyNoInteractions(compiler, runtime);
        }
    }

    @Test
    void rechecksProfilesExecutorAndTransactionManagerOnEveryCall() throws Exception {
        var compiler = mock(FmGroovyPreviewRunner.class);
        var runtime = mock(FmGroovyRuntimeRunner.class);
        var manager = mock(PlatformTransactionManager.class);
        var executor = mock(AsyncExecutor.class);
        var engine = new SpringProcessEngineConfiguration();
        engine.setTransactionManager(manager);
        engine.setAsyncExecutor(executor);
        when(executor.isActive()).thenReturn(true);
        var profile = new FmGroovyVersionManifest.EngineProfile(
                "5.0.6", "ExampleJDK", "21.0.10+7", "jvm:groovy-5.0.6-trusted1", "1");
        when(compiler.profile()).thenReturn(profile);
        when(runtime.profile()).thenReturn(profile);
        try (var context = new StaticApplicationContext()) {
            context.getBeanFactory().registerSingleton("engine", engine);
            var readiness = new FmGroovyRuntimeReadiness(compiler, runtime,
                    context.getBeanProvider(SpringProcessEngineConfiguration.class), manager);
            assertTrue(readiness.isReady());
            when(runtime.profile()).thenReturn(new FmGroovyVersionManifest.EngineProfile(
                    "5.0.6", "ExampleJDK", "21.0.10+7", "jvm:other-worker", "1"));
            assertFalse(readiness.isReady());
            when(runtime.profile()).thenThrow(new ServiceException("GROOVY_RUNTIME_UNAVAILABLE"));
            assertFalse(readiness.isReady());
            when(executor.isActive()).thenReturn(false);
            assertFalse(readiness.isReady());
            when(executor.isActive()).thenReturn(true);
            engine.setTransactionManager(mock(PlatformTransactionManager.class));
            assertFalse(readiness.isReady());
        }
    }
}
