package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.job.api.DeadLetterJobQuery;
import org.flowable.job.api.Job;
import org.flowable.job.api.JobQuery;
import org.flowable.job.api.SuspendedJobQuery;
import org.flowable.job.api.TimerJobQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyReadyJobProbe.State;
import org.qifu.fm.entity.FmSystemTaskExecution;

class FmGroovyReadyJobProbeTest {
    private final RuntimeService runtime = mock(RuntimeService.class);
    private final ManagementService management = mock(ManagementService.class);
    private final FmGroovyReadyJobProbe probe = new FmGroovyReadyJobProbe(runtime, management);
    private final ExecutionQuery executions = mock(ExecutionQuery.class);
    private final Execution execution = mock(Execution.class);
    private final ProcessInstance instance = mock(ProcessInstance.class);
    private final JobQuery active = mock(JobQuery.class);
    private final TimerJobQuery timers = mock(TimerJobQuery.class);
    private final SuspendedJobQuery suspended = mock(SuspendedJobQuery.class);
    private final DeadLetterJobQuery dead = mock(DeadLetterJobQuery.class);
    private final Job job = mock(Job.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();

    @BeforeEach
    void setup() throws Exception {
        when(runtime.createExecutionQuery()).thenReturn(executions);
        when(executions.executionId("execution")).thenReturn(executions);
        when(executions.singleResult()).thenReturn(execution);
        when(execution.getTenantId()).thenReturn("T");
        when(execution.getId()).thenReturn("execution");
        when(execution.getProcessInstanceId()).thenReturn("instance");
        when(execution.getActivityId()).thenReturn("node");
        var instances = mock(ProcessInstanceQuery.class);
        when(runtime.createProcessInstanceQuery()).thenReturn(instances);
        when(instances.processInstanceId("instance")).thenReturn(instances);
        when(instances.singleResult()).thenReturn(instance);
        when(instance.getTenantId()).thenReturn("T");
        when(instance.getId()).thenReturn("instance");
        when(instance.getProcessDefinitionId()).thenReturn("definition");
        when(management.createJobQuery()).thenReturn(active);
        when(active.executionId("execution")).thenReturn(active);
        when(active.listPage(0, 2)).thenReturn(List.of());
        when(management.createTimerJobQuery()).thenReturn(timers);
        when(timers.executionId("execution")).thenReturn(timers);
        when(timers.listPage(0, 2)).thenReturn(List.of());
        when(management.createSuspendedJobQuery()).thenReturn(suspended);
        when(suspended.executionId("execution")).thenReturn(suspended);
        when(suspended.listPage(0, 2)).thenReturn(List.of());
        when(management.createDeadLetterJobQuery()).thenReturn(dead);
        when(dead.executionId("execution")).thenReturn(dead);
        when(dead.listPage(0, 2)).thenReturn(List.of());
        when(job.getTenantId()).thenReturn("T");
        when(job.getId()).thenReturn("job");
        when(job.getExecutionId()).thenReturn("execution");
        when(job.getProcessInstanceId()).thenReturn("instance");
        when(job.getProcessDefinitionId()).thenReturn("definition");
        when(job.getElementId()).thenReturn("node");
        when(job.getJobHandlerType()).thenReturn("async-continuation");
        when(job.getJobType()).thenReturn(Job.JOB_TYPE_MESSAGE);
        when(job.isExclusive()).thenReturn(true);
        when(job.getCorrelationId()).thenReturn("occurrence");
        row.setTenantId("T");
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("node");
        row.setInvocationId(new FmGroovyJobIdentity("T", "execution", "instance", "definition", "node").invocation(job));
    }

    @Test
    void allQueueTypesAreDistinguishedWithoutExecutingOrDeletingJobs() throws Exception {
        assertEquals(State.MISSING, probe.inspect(row, "definition"));
        when(active.listPage(0, 2)).thenReturn(List.of(job));
        assertEquals(State.WAITING, probe.inspect(row, "definition"));
        when(active.listPage(0, 2)).thenReturn(List.of());
        when(timers.listPage(0, 2)).thenReturn(List.of(job));
        assertEquals(State.WAITING, probe.inspect(row, "definition"));
        when(timers.listPage(0, 2)).thenReturn(List.of());
        when(suspended.listPage(0, 2)).thenReturn(List.of(job));
        assertEquals(State.WAITING, probe.inspect(row, "definition"));
        when(suspended.listPage(0, 2)).thenReturn(List.of());
        when(dead.listPage(0, 2)).thenReturn(List.of(job));
        assertEquals(State.DEAD_LETTER, probe.inspect(row, "definition"));
        verify(management, never()).executeJob(any());
        verify(management, never()).deleteJob(any());
        verify(management, never()).deleteDeadLetterJob(any());
    }

    @Test
    void removedEndedAndAdvancedExecutionOnlyRetireTheOldOccurrence() throws Exception {
        when(executions.singleResult()).thenReturn(null);
        assertEquals(State.EXECUTION_REMOVED, probe.inspect(row, "definition"));
        when(executions.singleResult()).thenReturn(execution);
        when(execution.isEnded()).thenReturn(true);
        assertEquals(State.EXECUTION_REMOVED, probe.inspect(row, "definition"));
        when(execution.isEnded()).thenReturn(false);
        when(execution.getActivityId()).thenReturn("next-node");
        assertEquals(State.EXECUTION_REMOVED, probe.inspect(row, "definition"));
        verifyNoInteractions(management);
    }

    @Test
    void explicitSuspensionNeverBecomesAMissingJobFailure() throws Exception {
        when(instance.isSuspended()).thenReturn(true);
        assertEquals(State.WAITING, probe.inspect(row, "definition"));
        verifyNoInteractions(management);
    }

    @Test
    void aNewLoopOccurrenceIsPreservedWhileOldLedgerIsRetired() throws Exception {
        when(active.listPage(0, 2)).thenReturn(List.of(job));
        when(job.getCorrelationId()).thenReturn("new-loop-occurrence");
        assertEquals(State.EXECUTION_REMOVED, probe.inspect(row, "definition"));
        verify(management, never()).deleteJob(any());
    }

    @Test
    void foreignIdentityAndAmbiguousJobsCannotAuthorizeRecovery() throws Exception {
        when(active.listPage(0, 2)).thenReturn(List.of(job, job));
        assertThrows(ServiceException.class, () -> probe.inspect(row, "definition"));
        when(active.listPage(0, 2)).thenReturn(List.of(job));
        when(job.getTenantId()).thenReturn("U");
        assertThrows(ServiceException.class, () -> probe.inspect(row, "definition"));
        when(execution.getTenantId()).thenReturn("U");
        assertThrows(ServiceException.class, () -> probe.inspect(row, "definition"));
    }
}
