package org.qifu.fm.domain.workflow;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.qifu.base.exception.ServiceException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/** Checks the deployed engine configuration; publication still validates and persists its version. */
@Component
public class FmGroovyRuntimeReadiness implements FmGroovyPublishAccess.RuntimeReadiness {

    private final FmGroovyPreviewRunner compilerRunner;
    private final FmGroovyRuntimeRunner runtimeRunner;
    private final ObjectProvider<SpringProcessEngineConfiguration> engines;
    private final PlatformTransactionManager transactionManager;

    public FmGroovyRuntimeReadiness(FmGroovyPreviewRunner compilerRunner,
            FmGroovyRuntimeRunner runtimeRunner, ObjectProvider<SpringProcessEngineConfiguration> engines,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        this.compilerRunner = compilerRunner;
        this.runtimeRunner = runtimeRunner;
        this.engines = engines;
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean isReady() {
        var engine = engines.getIfUnique();
        if (engine == null || engine.getTransactionManager() != transactionManager
                || engine.getAsyncExecutor() == null || !engine.getAsyncExecutor().isActive()) {
            return false;
        }
        try {
            return compilerRunner.profile().equals(runtimeRunner.profile());
        } catch (ServiceException unavailable) {
            return false;
        }
    }
}
