package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.engine.ManagementService;
import org.flowable.job.api.DeadLetterJobQuery;
import org.flowable.job.api.Job;
import org.flowable.job.api.JobQuery;
import org.flowable.job.api.SuspendedJobQuery;
import org.flowable.job.api.TimerJobQuery;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.asyncexecutor.DefaultJobManager;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntityImpl;
import org.flowable.job.service.impl.persistence.entity.JobEntityImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;

class FmGroovyRetryJobTest {
    private final ManagementService management = mock(ManagementService.class);
    private final FmGroovyFlowableTransactionGuard guard = mock(FmGroovyFlowableTransactionGuard.class);
    private final FmGroovyRetryJob adapter = new FmGroovyRetryJob(management, guard);
    private final JobQuery active = mock(JobQuery.class);
    private final DeadLetterJobQuery dead = mock(DeadLetterJobQuery.class);
    private final Job job = mock(Job.class);
    private final Job restored = mock(Job.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();

    @BeforeEach
    void setup() throws Exception {
        var timer = mock(TimerJobQuery.class);
        var suspended = mock(SuspendedJobQuery.class);
        when(management.createJobQuery()).thenReturn(active);
        when(active.executionId("execution")).thenReturn(active);
        when(management.createTimerJobQuery()).thenReturn(timer);
        when(timer.executionId("execution")).thenReturn(timer);
        when(management.createSuspendedJobQuery()).thenReturn(suspended);
        when(suspended.executionId("execution")).thenReturn(suspended);
        when(management.createDeadLetterJobQuery()).thenReturn(dead);
        when(dead.executionId("execution")).thenReturn(dead);
        when(dead.listPage(0, 2)).thenReturn(List.of(job));
        for (var candidate : List.of(job, restored)) {
            when(candidate.getTenantId()).thenReturn("T");
            when(candidate.getExecutionId()).thenReturn("execution");
            when(candidate.getProcessInstanceId()).thenReturn("instance");
            when(candidate.getProcessDefinitionId()).thenReturn("definition");
            when(candidate.getElementId()).thenReturn("node");
            when(candidate.getCorrelationId()).thenReturn("occurrence");
            when(candidate.getJobHandlerType()).thenReturn("async-continuation");
            when(candidate.getJobType()).thenReturn(Job.JOB_TYPE_MESSAGE);
            when(candidate.isExclusive()).thenReturn(true);
        }
        when(job.getId()).thenReturn("dead-id");
        when(restored.getId()).thenReturn("new-id");
        when(restored.getRetries()).thenReturn(1);
        row.setTenantId("T");
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("node");
        row.setInvocationId(new FmGroovyJobIdentity("T", "execution", "instance", "definition", "node").invocation(job));
        when(management.executeCommand(any(Command.class))).thenAnswer(call -> ((Command<?>) call.getArgument(0)).execute(null));
        when(management.moveDeadLetterJobToExecutableJob("dead-id", 1)).thenReturn(restored);
    }

    @Test
    void changedTechnicalIdPreservesOccurrenceAndSchedulesOnlyOneExecution() throws Exception {
        adapter.check(row, "definition");
        verify(management, never()).moveDeadLetterJobToExecutableJob("dead-id", 1);
        adapter.restore(row, "definition");
        verify(guard).require();
        verify(management).moveDeadLetterJobToExecutableJob("dead-id", 1);
        verify(management, never()).executeJob(any());
    }

    @Test
    void activeOrAmbiguousJobsCannotBeRestored() throws Exception {
        when(active.count()).thenReturn(1L);
        assertThrows(ServiceException.class, () -> adapter.restore(row, "definition"));
        when(active.count()).thenReturn(0L);
        when(dead.listPage(0, 2)).thenReturn(List.of(job, job));
        assertThrows(ServiceException.class, () -> adapter.restore(row, "definition"));
        verify(management, never()).moveDeadLetterJobToExecutableJob(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void wrongTenantNodeOrCorrelationFailsClosed() throws Exception {
        when(job.getTenantId()).thenReturn("U");
        assertThrows(ServiceException.class, () -> adapter.check(row, "definition"));
        when(job.getTenantId()).thenReturn("T");
        when(job.getElementId()).thenReturn("other");
        assertThrows(ServiceException.class, () -> adapter.check(row, "definition"));
        when(job.getElementId()).thenReturn("node");
        when(job.getCorrelationId()).thenReturn("other");
        assertThrows(ServiceException.class, () -> adapter.check(row, "definition"));
    }

    @Test
    void movementThatLosesCorrelationOrAddsRetriesFails() throws Exception {
        when(restored.getCorrelationId()).thenReturn("other");
        assertThrows(ServiceException.class, () -> adapter.restore(row, "definition"));
        when(restored.getCorrelationId()).thenReturn("occurrence");
        when(restored.getRetries()).thenReturn(3);
        assertThrows(ServiceException.class, () -> adapter.restore(row, "definition"));
    }

    @Test
    void recalculationUsesNewCorrelationCopiedByActualFlowableJobManager() throws Exception {
        var entity = new DeadLetterJobEntityImpl();
        entity.setId("dead-id");
        entity.setTenantId("T");
        entity.setExecutionId("execution");
        entity.setProcessInstanceId("instance");
        entity.setProcessDefinitionId("definition");
        entity.setElementId("node");
        entity.setCorrelationId("occurrence");
        entity.setJobHandlerType("async-continuation");
        entity.setJobType(Job.JOB_TYPE_MESSAGE);
        entity.setExclusive(true);
        when(dead.listPage(0, 2)).thenReturn(List.of(entity));
        var copied = new JobEntityImpl();
        when(management.moveDeadLetterJobToExecutableJob("dead-id", 1)).thenAnswer(call -> {
            new DefaultJobManager(new JobServiceConfiguration("recalculate-test")).copyJobInfo(copied, entity);
            copied.setId("new-id");
            copied.setRetries(1);
            return copied;
        });
        String original = row.getInvocationId();
        assertEquals("new-id", adapter.recalculate(row, "definition", "new-correlation"));
        assertEquals("new-correlation", copied.getCorrelationId());
        String derived = new FmGroovyJobIdentity("T", "execution", "instance", "definition", "node").invocation(copied);
        assertNotEquals(original, derived);
        assertEquals(FmGroovyJobIdentity.invocationForCorrelation("T", "new-correlation"), derived);
        assertEquals(original, row.getInvocationId());
        verify(guard).require();
        verify(management, never()).executeJob(any());
    }

    @Test
    void recalculationRejectsSameOccurrenceAndUnmanagedJob() throws Exception {
        assertThrows(ServiceException.class, () -> adapter.recalculate(row, "definition", "occurrence"));
        // Job API projection without the command's managed entity is not safe to mutate.
        assertThrows(ServiceException.class, () -> adapter.recalculate(row, "definition", "new-correlation"));
        verify(management, never()).moveDeadLetterJobToExecutableJob(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void wrongTransactionManagerPreventsEngineMutation() throws Exception {
        doThrow(new ServiceException("wrong transaction")).when(guard).require();
        assertThrows(ServiceException.class, () -> adapter.restore(row, "definition"));
        verify(management, never()).moveDeadLetterJobToExecutableJob(any(), org.mockito.ArgumentMatchers.anyInt());
    }
}
