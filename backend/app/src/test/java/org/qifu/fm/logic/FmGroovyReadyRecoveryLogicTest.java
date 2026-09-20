package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyReadyJobProbe;
import org.qifu.fm.domain.workflow.FmGroovyReadyJobProbe.State;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.impl.FmGroovyReadyRecoveryLogicServiceImpl;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

class FmGroovyReadyRecoveryLogicTest {
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final FmGroovyReadyJobProbe jobs = mock(FmGroovyReadyJobProbe.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final FmProcessInstance process = new FmProcessInstance();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private final Date now = Date.from(GroovyRuntimeFixture.START.plusSeconds(120));
    private final Date cutoff = Date.from(GroovyRuntimeFixture.START);
    private IFmGroovyReadyRecoveryLogicService logic;

    @BeforeEach
    void setup() throws Exception {
        logic = transactions.proxy((IFmGroovyReadyRecoveryLogicService) new FmGroovyReadyRecoveryLogicServiceImpl(
                executions, processes, jobs, Clock.fixed(now.toInstant(), ZoneOffset.UTC)));
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId("invocation");
        row.setProcessInstanceId("instance");
        row.setTaskType("GROOVY");
        row.setStatus("READY");
        row.setGeneration(0);
        row.setAttemptNo(0);
        row.setStartedAt(cutoff);
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("RUNNING");
        process.setFlowableProcessDefId("definition");
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(jobs.inspect(row, "definition")).thenReturn(State.DEAD_LETTER);
        when(executions.finishReady(eq("T"), eq("ledger"), eq(0), eq(cutoff), eq(now), any(), any())).thenReturn(1);
    }

    @Test
    void failedStartBeforeFirstAttemptEndsReadyAtGraceBoundary() throws Exception {
        assertTrue(logic.recover("T", "invocation", 0));
        verify(executions).finishReady("T", "ledger", 0, cutoff, now, "FAILED", "GROOVY_START_FAILED");
        assertEquals(0, row.getAttemptNo());
        verify(executions, never()).claim(any(), any(), anyInt(), any(), any());
        verify(executions, never()).insert(any());
        assertEquals(1, transactions.commits);
    }

    @Test
    void missingJobBecomesVisibleFailureAndRemovedExecutionBecomesCancellation() throws Exception {
        when(jobs.inspect(row, "definition")).thenReturn(State.MISSING);
        assertTrue(logic.recover("T", "invocation", 0));
        verify(executions).finishReady("T", "ledger", 0, cutoff, now, "FAILED", "GROOVY_JOB_MISSING");
        when(jobs.inspect(row, "definition")).thenReturn(State.EXECUTION_REMOVED);
        assertTrue(logic.recover("T", "invocation", 0));
        verify(executions).finishReady("T", "ledger", 0, cutoff, now, "CANCELLED", "GROOVY_EXECUTION_REMOVED");
    }

    @Test
    void terminalProcessDoesNotNeedAnEngineJobToCancelUnstartedWork() throws Exception {
        process.setInstanceStatus("TERMINATED");
        assertTrue(logic.recover("T", "invocation", 0));
        verify(jobs, never()).inspect(any(), any());
        verify(executions).finishReady("T", "ledger", 0, cutoff, now, "CANCELLED", "GROOVY_PROCESS_ENDED_BEFORE_START");
    }

    @Test
    void activeQueuedOrSuspendedWorkIsPreserved() throws Exception {
        when(jobs.inspect(row, "definition")).thenReturn(State.WAITING);
        assertFalse(logic.recover("T", "invocation", 0));
        verify(executions, never()).finishReady(any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void newRetryDueTimeNotOriginalStartControlsGracePeriod() throws Exception {
        row.setNextAttemptAt(new Date(cutoff.getTime() + 1));
        assertFalse(logic.recover("T", "invocation", 0));
        row.setNextAttemptAt(new Date(now.getTime() + 10000));
        assertFalse(logic.recover("T", "invocation", 0));
        verify(jobs, never()).inspect(any(), any());
    }

    @Test
    void claimAndNewGenerationWinningTheLockMakeRecoveryANoOp() throws Exception {
        for (String state : List.of("RUNNING", "FAILED", "SUCCEEDED", "CANCELLED")) {
            row.setStatus(state);
            assertFalse(logic.recover("T", "invocation", 0));
        }
        row.setStatus("READY");
        row.setGeneration(1);
        assertFalse(logic.recover("T", "invocation", 0));
        verify(jobs, never()).inspect(any(), any());
        verify(executions, never()).finishReady(any(), any(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void lostCasRollsBackRatherThanReportingRecovered() throws Exception {
        when(executions.finishReady(any(), any(), anyInt(), any(), any(), any(), any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.recover("T", "invocation", 0));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test
    void wrongTenantMalformedLedgerAndUnknownProcessStateFailClosed() throws Exception {
        row.setTenantId("U");
        assertThrows(ServiceException.class, () -> logic.recover("T", "invocation", 0));
        row.setTenantId("T");
        row.setLeaseUntil(now);
        assertThrows(ServiceException.class, () -> logic.recover("T", "invocation", 0));
        row.setLeaseUntil(null);
        process.setInstanceStatus("UNKNOWN");
        assertThrows(ServiceException.class, () -> logic.recover("T", "invocation", 0));
        verify(executions, never()).finishReady(any(), any(), anyInt(), any(), any(), any(), any());
    }
}
