package org.qifu.fm.logic;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/** Tests actual Spring transaction boundaries; it does not simulate database atomicity. */
final class GroovyTransactionFixture extends AbstractPlatformTransactionManager {

    private static final long serialVersionUID = 1L;
    private final ThreadLocal<State> current = new ThreadLocal<>();
    int commits;
    int rollbacks;

    @SuppressWarnings("unchecked")
    <T> T proxy(T target) {
        var factory = new ProxyFactory(target);
        factory.addAdvice(new TransactionInterceptor(this, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }

    @Override
    protected Object doGetTransaction() {
        return current.get() == null ? new State() : current.get();
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        return ((State) transaction).active;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        ((State) transaction).active = true;
        current.set((State) transaction);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        commits++;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        rollbacks++;
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        // The owning TransactionTemplate rethrows the tested failure and rolls back.
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        ((State) transaction).active = false;
        current.remove();
    }

    private static final class State {
        private boolean active;
    }
}
