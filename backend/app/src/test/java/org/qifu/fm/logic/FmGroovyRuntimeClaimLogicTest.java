package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.flowable.engine.ManagementService;
import org.flowable.job.api.Job;
import org.flowable.job.api.JobQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.impl.FmGroovyRuntimeClaimLogicServiceImpl;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.entity.FmProcessInstance;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class FmGroovyRuntimeClaimLogicTest {

    private final ManagementService management = mock(ManagementService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final FmProcessInstance process = new FmProcessInstance();
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final Job job = mock(Job.class);
    private final JobQuery query = mock(JobQuery.class);
    private final FmGroovyJobIdentity identity = new FmGroovyJobIdentity("T", "execution", "instance", "deployed:1", "calculate");
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyRuntimeClaimLogicService logic;

    @BeforeEach
    void fixture() throws Exception {
        logic = transactions.proxy((IFmGroovyRuntimeClaimLogicService) new FmGroovyRuntimeClaimLogicServiceImpl(
                management, inputs, executions, attempts, runner, processes,
                Clock.fixed(GroovyRuntimeFixture.START.plusNanos(123456789), ZoneOffset.UTC)));
        when(management.createJobQuery()).thenReturn(query);
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("RUNNING");
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(query.executionId("execution")).thenReturn(query);
        when(query.listPage(0, 2)).thenReturn(List.of(job));
        when(job.getId()).thenReturn("job-1");
        when(job.getCorrelationId()).thenReturn("occurrence-1");
        when(job.getTenantId()).thenReturn("T");
        when(job.getExecutionId()).thenReturn("execution");
        when(job.getProcessInstanceId()).thenReturn("instance");
        when(job.getProcessDefinitionId()).thenReturn("deployed:1");
        when(job.getElementId()).thenReturn("calculate");
        when(job.getJobType()).thenReturn("message");
        when(job.getJobHandlerType()).thenReturn("async-continuation");
        when(job.isExclusive()).thenReturn(true);
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of()));
        when(inputs.load(eq("T"), eq("execution"), any(), any(), eq(GroovyRuntimeFixture.PROFILE)))
                .thenAnswer(call -> GroovyRuntimeFixture.loaded(call.getArgument(2), call.getArgument(3)));
        when(executions.insert(any())).thenAnswer(call -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            FmSystemTaskExecution row = call.getArgument(0);
            row.setOid("ledger");
            return value(row);
        });
        when(executions.claim(eq("T"), eq("ledger"), eq(0), any(), any())).thenReturn(1);
        when(attempts.insert(any())).thenAnswer(call -> value(call.getArgument(0)));
    }

    @Test
    void commitsInitialSnapshotClaimAndAttemptTogetherWithMillisecondTime() throws Exception {
        var claim = logic.begin(identity);
        assertEquals(identity.invocation(job), claim.invocationId());
        assertEquals(1, claim.generation());
        var captured = ArgumentCaptor.forClass(FmSystemTaskExecution.class);
        verify(executions).insert(captured.capture());
        var row = captured.getValue();
        assertEquals("job-1", row.getFirstJobId());
        assertEquals(GroovyRuntimeFixture.START.plusMillis(123), row.getStartedAt().toInstant());
        assertTrue(row.getContextContent().contains("2026-09-12T00:00:00.123Z"));
        var capturedAttempt = ArgumentCaptor.forClass(FmSystemTaskAttempt.class);
        verify(attempts).insert(capturedAttempt.capture());
        assertEquals(60000, capturedAttempt.getValue().getLeaseUntil().getTime() - row.getStartedAt().getTime());
        assertEquals(claim.attemptId(), capturedAttempt.getValue().getRequestId());
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
    }

    @Test
    void revokedFirstAdmissionStillCreatesDurableAttemptWithoutWorkerIo() throws Exception {
        when(runner.profile()).thenThrow(new ServiceException("ENGINE_PROFILE_DISABLED"));
        when(runner.profileForFailureRecording()).thenReturn(GroovyRuntimeFixture.PROFILE);
        var claim = logic.begin(identity);
        assertEquals(1, claim.generation());
        verify(executions).insert(any());
        verify(attempts).insert(any());
        verify(runner, never()).execute(any(), any(), any(), anyInt());
        assertEquals(1, transactions.commits);
    }

    @Test
    void disabledRuntimeDoesNotUseFailureRecordingBypass() throws Exception {
        when(runner.profile()).thenThrow(new ServiceException("GROOVY_RUNTIME_UNAVAILABLE"));
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        verify(runner, never()).profileForFailureRecording();
        verify(executions, never()).insert(any());
    }

    @Test
    void correlationSurvivesTechnicalJobMoveButLoopAndTenantAreDistinct() throws Exception {
        String first = identity.invocation(job);
        when(job.getId()).thenReturn("restored-job-2");
        assertEquals(first, identity.invocation(job));
        when(job.getCorrelationId()).thenReturn("occurrence-2");
        assertNotEquals(first, identity.invocation(job));
        when(job.getTenantId()).thenReturn("OTHER");
        assertThrows(ServiceException.class, () -> identity.invocation(job));
    }

    @Test
    void ambiguousOrUnrelatedJobCannotCreateLedger() throws Exception {
        when(query.listPage(0, 2)).thenReturn(List.of(job, job));
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        when(query.listPage(0, 2)).thenReturn(List.of(job));
        when(job.getElementId()).thenReturn("another-node");
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        verify(executions, never()).insert(any());
    }

    @Test
    void failedAttemptInsertOrLostClaimRollsBackWholeInitialization() throws Exception {
        doThrow(new ServiceException("ATTEMPT_INSERT_FAILED")).when(attempts).insert(any());
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
        when(executions.claim(eq("T"), eq("ledger"), eq(0), any(), any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        assertEquals(2, transactions.rollbacks);
    }

    @Test
    void repeatedRunningOccurrenceCannotStartAnotherAttempt() throws Exception {
        var row = new FmSystemTaskExecution();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId(identity.invocation(job));
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("calculate");
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setStatus("RUNNING");
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", row.getInvocationId())).thenReturn(row);
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        verify(attempts, never()).insert(any());
        verify(executions, never()).claim(any(), any(), anyInt(), any(), any());
    }

    @Test
    void lateFailureDoesNotOverwriteSucceededOrCancelledGeneration() throws Exception {
        var claim = new IFmGroovyRuntimeClaimLogicService.Claim("T", "ledger", "invocation", "attempt", 1);
        var row = new FmSystemTaskExecution();
        row.setOid("ledger");
        row.setGeneration(1);
        row.setStatus("SUCCEEDED");
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        logic.fail(claim, "GROOVY_RUNTIME_ERROR");
        row.setStatus("RUNNING");
        row.setGeneration(2);
        logic.fail(claim, "GROOVY_RUNTIME_ERROR");
        verify(attempts, never()).finish(any(), any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void cancellationWinningProcessLockPreventsLateClaim() throws Exception {
        process.setInstanceStatus("CANCELLED");
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        verify(executions, never()).insert(any());
        verify(attempts, never()).insert(any());
    }

    @Test
    void readyOccurrenceCannotClaimAFourthAttempt() throws Exception {
        var row = new FmSystemTaskExecution();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId(identity.invocation(job));
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("calculate");
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setStatus("READY");
        row.setGeneration(6);
        row.setAttemptNo(3);
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", row.getInvocationId())).thenReturn(row);
        assertThrows(ServiceException.class, () -> logic.begin(identity));
        verify(attempts, never()).insert(any());
        verify(executions, never()).claim(any(), any(), anyInt(), any(), any());
    }

    @Test
    void rearmedOccurrenceClaimsBeforeEvaluationSoInputFailureCanBeRecorded() throws Exception {
        var row = new FmSystemTaskExecution();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId(identity.invocation(job));
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("calculate");
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setStatus("READY");
        row.setGeneration(2);
        row.setAttemptNo(1);
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", row.getInvocationId())).thenReturn(row);
        when(executions.claim(eq("T"), eq("ledger"), eq(2), any(), any())).thenReturn(1);
        var claim = logic.begin(identity);
        assertEquals(3, claim.generation());
        assertEquals(row.getInvocationId(), claim.invocationId());
        verify(inputs, never()).load(any(), any(), any(), any(), any());
        verify(attempts).insert(any());
    }

    private static <T> DefaultResult<T> value(T row) {
        var result = new DefaultResult<T>();
        result.setValue(row);
        return result;
    }
}
