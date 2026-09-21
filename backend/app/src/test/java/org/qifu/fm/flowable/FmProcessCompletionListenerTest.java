package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.event.FlowableProcessEngineEvent;
import org.junit.jupiter.api.Test;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.beans.factory.ObjectProvider;

class FmProcessCompletionListenerTest {

    private static final Instant NOW = Instant.parse("2026-09-20T10:00:00Z");

    @Test
    void completedProcessTransitionsRunningProjectionInSameCallback() {
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        ObjectProvider<IFmProcessInstanceService> provider = provider(processes);
        FlowableProcessEngineEvent event = mock(FlowableProcessEngineEvent.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(event.getType()).thenReturn(FlowableEngineEventType.PROCESS_COMPLETED);
        when(event.getExecution()).thenReturn(execution);
        when(execution.getTenantId()).thenReturn("A01");
        when(execution.getProcessInstanceId()).thenReturn("P1");

        new FmProcessCompletionListener(provider, Clock.fixed(NOW, ZoneOffset.UTC)).onEvent(event);

        verify(processes).updateStatus("A01", "P1", "RUNNING", "COMPLETED",
                Date.from(NOW), FmProcessCompletionListener.UPDATE_ACCOUNT);
    }

    @Test
    void unrelatedAndUnmanagedEventsAreIgnored() {
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        ObjectProvider<IFmProcessInstanceService> provider = provider(processes);
        FlowableEvent unrelated = mock(FlowableEvent.class);
        when(unrelated.getType()).thenReturn(FlowableEngineEventType.TASK_COMPLETED);
        new FmProcessCompletionListener(provider, Clock.fixed(NOW, ZoneOffset.UTC)).onEvent(unrelated);

        FlowableProcessEngineEvent missingTenant = mock(FlowableProcessEngineEvent.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        when(missingTenant.getType()).thenReturn(FlowableEngineEventType.PROCESS_COMPLETED);
        when(missingTenant.getExecution()).thenReturn(execution);
        when(execution.getTenantId()).thenReturn(" ");
        when(execution.getProcessInstanceId()).thenReturn("P1");
        new FmProcessCompletionListener(provider, Clock.fixed(NOW, ZoneOffset.UTC)).onEvent(missingTenant);

        verify(processes, never()).updateStatus(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void subscribesOnlyToNormalProcessCompletionAndFailsTransactionOnError() {
        var listener = new FmProcessCompletionListener(provider(mock(IFmProcessInstanceService.class)),
                Clock.fixed(NOW, ZoneOffset.UTC));

        assertEquals(java.util.List.of(FlowableEngineEventType.PROCESS_COMPLETED), listener.getTypes());
        assertEquals(true, listener.isFailOnException());
        assertEquals(false, listener.isFireOnTransactionLifecycleEvent());
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<IFmProcessInstanceService> provider(IFmProcessInstanceService service) {
        ObjectProvider<IFmProcessInstanceService> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(service);
        return provider;
    }
}
