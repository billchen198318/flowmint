package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.flowable.FmGroovyExecutionDeletionListener;
import org.qifu.fm.flowable.FmGroovyExecutionDeletionConfiguration;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class FmGroovyExecutionDeletionListenerTest {

    private final IFmGroovyCancellationLogicService cancellation = mock(IFmGroovyCancellationLogicService.class);
    private final FmGroovyFlowableTransactionGuard guard = mock(FmGroovyFlowableTransactionGuard.class);

    private FmGroovyExecutionDeletionListener listener() {
        var beans = new StaticListableBeanFactory();
        beans.addBean("cancellation", cancellation);
        beans.addBean("guard", guard);
        return new FmGroovyExecutionDeletionListener(beans.getBeanProvider(IFmGroovyCancellationLogicService.class),
                beans.getBeanProvider(FmGroovyFlowableTransactionGuard.class));
    }

    @Test
    void configurationPreservesExistingListenersAndDefersServiceResolution() {
        var beans = new StaticListableBeanFactory();
        var configuration = new SpringProcessEngineConfiguration();
        var existing = listener();
        configuration.setEventListeners(java.util.List.of(existing));
        new FmGroovyExecutionDeletionConfiguration().groovyExecutionDeletionConfigurer(
                beans.getBeanProvider(IFmGroovyCancellationLogicService.class),
                beans.getBeanProvider(FmGroovyFlowableTransactionGuard.class)).configure(configuration);
        org.junit.jupiter.api.Assertions.assertEquals(2, configuration.getEventListeners().size());
        org.junit.jupiter.api.Assertions.assertSame(existing, configuration.getEventListeners().getFirst());
    }

    @Test
    void deletionUsesEngineIdentityAndSynchronousFailClosedCallback() throws Exception {
        var listener = listener();
        listener.onEvent(event());
        verify(guard).require();
        verify(cancellation).cancelForExecution("T", "instance", "execution");
        assertTrue(listener.isFailOnException());
        assertFalse(listener.isFireOnTransactionLifecycleEvent());
    }

    @Test
    void unrelatedEntityAndEventAreIgnored() {
        var event = event();
        when(event.getEntity()).thenReturn(new Object());
        listener().onEvent(event);
        event = event();
        when(event.getType()).thenReturn(FlowableEngineEventType.ENTITY_CREATED);
        listener().onEvent(event);
        verifyNoInteractions(guard, cancellation);
    }

    @Test
    void transactionMismatchPreventsLedgerMutation() throws Exception {
        doThrow(new ServiceException("mismatch")).when(guard).require();
        assertThrows(IllegalStateException.class, () -> listener().onEvent(event()));
        verifyNoInteractions(cancellation);
    }

    @Test
    void ledgerFailurePropagatesToEngine() throws Exception {
        doThrow(new ServiceException("failure")).when(cancellation)
                .cancelForExecution("T", "instance", "execution");
        assertThrows(IllegalStateException.class, () -> listener().onEvent(event()));
    }

    private FlowableEntityEvent event() {
        var execution = new ExecutionEntityImpl();
        execution.setId("execution");
        execution.setTenantId("T");
        execution.setProcessInstanceId("instance");
        var event = mock(FlowableEntityEvent.class);
        when(event.getType()).thenReturn(FlowableEngineEventType.ENTITY_DELETED);
        when(event.getEntity()).thenReturn(execution);
        return event;
    }
}
