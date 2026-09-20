package org.qifu.fm.domain.workflow;

import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.qifu.base.exception.ServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Fail closed unless afterExecution is inside the engine's shared application transaction. */
@Component
public class FmGroovyFlowableTransactionGuard {

    private final PlatformTransactionManager transactionManager;

    public FmGroovyFlowableTransactionGuard(@Qualifier("transactionManager") PlatformTransactionManager manager) {
        transactionManager = manager;
    }

    public void require() throws ServiceException {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                || CommandContextUtil.getCommandContext() == null
                || !(CommandContextUtil.getProcessEngineConfiguration() instanceof SpringProcessEngineConfiguration config)
                || config.getTransactionManager() != transactionManager) {
            throw new ServiceException("GROOVY_FLOWABLE_TRANSACTION_REQUIRED");
        }
    }
}
