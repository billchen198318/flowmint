package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.impl.FmGroovyCancellationLogicServiceImpl;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

class FmGroovyCancellationLogicTest {

    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private final FmProcessInstance process = new FmProcessInstance();
    private final Clock clock = Clock.fixed(GroovyRuntimeFixture.START, ZoneOffset.UTC);
    private IFmGroovyCancellationLogicService logic;

    @BeforeEach
    void fixture() throws Exception {
        logic = transactions.proxy((IFmGroovyCancellationLogicService) new FmGroovyCancellationLogicServiceImpl(
                processes, executions, attempts, true, clock));
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("CANCELLED");
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(executions.lockActiveForProcess("T", "instance"))
                .thenReturn(List.of(row("ready", "READY", 0), row("running", "RUNNING", 1)));
        when(attempts.cancelRunning(any(), any(), anyInt(), any())).thenAnswer(call ->
                "running".equals(call.getArgument(1)) ? 1 : 0);
        when(executions.cancel(any(), any(), any())).thenReturn(1);
    }

    @Test
    void readyAndRunningAreCancelledInCallerTransaction() throws Exception {
        cancel();
        verify(executions).cancel(eq("T"), eq("ready"), any());
        verify(executions).cancel(eq("T"), eq("running"), any());
        assertEquals(1, transactions.commits);
        assertThrows(IllegalTransactionStateException.class, () -> logic.cancelForProcess("T", "instance"));
    }

    @Test
    void repeatedCancellationWithNoActiveWorkIsNoOp() {
        when(executions.lockActiveForProcess("T", "instance")).thenReturn(List.of());
        cancel();
        verifyNoInteractions(attempts);
    }

    @Test
    void runningProcessOrForeignLedgerCannotBeCancelled() throws Exception {
        process.setInstanceStatus("RUNNING");
        assertThrows(RuntimeException.class, this::cancel);
        process.setInstanceStatus("TERMINATED");
        var foreign = row("running", "RUNNING", 1);
        foreign.setTenantId("OTHER");
        when(executions.lockActiveForProcess("T", "instance")).thenReturn(List.of(foreign));
        assertThrows(RuntimeException.class, this::cancel);
        verify(executions, never()).cancel(any(), any(), any());
    }

    @Test
    void missingAttemptOrLostExecutionMutationRollsBack() throws Exception {
        when(executions.lockActiveForProcess("T", "instance")).thenReturn(List.of(row("running", "RUNNING", 1)));
        when(attempts.cancelRunning(any(), any(), anyInt(), any())).thenReturn(0);
        assertThrows(RuntimeException.class, this::cancel);
        when(attempts.cancelRunning(any(), any(), anyInt(), any())).thenReturn(1);
        when(executions.cancel(any(), any(), any())).thenReturn(0);
        assertThrows(RuntimeException.class, this::cancel);
        assertEquals(0, transactions.commits);
        assertEquals(2, transactions.rollbacks);
    }

    @Test
    void disabledFeatureDoesNotTouchUnmigratedRuntimeTables() throws Exception {
        new FmGroovyCancellationLogicServiceImpl(processes, executions, attempts, false, clock)
                .cancelForProcess("T", "instance");
        verifyNoInteractions(processes, executions, attempts);
    }

    private void cancel() {
        new TransactionTemplate(transactions).execute(status -> {
            try {
                logic.cancelForProcess("T", "instance");
            } catch (ServiceException failure) {
                throw new RuntimeException(failure);
            }
            return null;
        });
    }

    @Test
    void engineDeletionCancelsOnlyMatchingExecutionWhileProcessStillRunning() {
        process.setInstanceStatus("RUNNING");
        var matching = row("running", "RUNNING", 1);
        matching.setFlowableExecutionId("deleted");
        var parallel = row("parallel", "RUNNING", 1);
        parallel.setFlowableExecutionId("other");
        when(executions.lockActiveForProcess("T", "instance")).thenReturn(List.of(matching, parallel));
        deleteExecution();
        verify(executions).cancel(eq("T"), eq("running"), any());
        verify(executions, never()).cancel(eq("T"), eq("parallel"), any());
        assertEquals(1, transactions.commits);
    }

    @Test
    void engineDeletionFailureRollsBackCallerTransaction() throws Exception {
        var matching = row("running", "RUNNING", 1);
        matching.setFlowableExecutionId("deleted");
        when(executions.lockActiveForProcess("T", "instance")).thenReturn(List.of(matching));
        when(executions.cancel(any(), any(), any())).thenReturn(0);
        assertThrows(RuntimeException.class, this::deleteExecution);
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test
    void engineDeletionWithoutFlowMintProcessDoesNotReadLedger() {
        when(processes.lockInstance("T", "instance")).thenReturn(null);
        deleteExecution();
        verifyNoInteractions(executions, attempts);
    }

    private void deleteExecution() {
        new TransactionTemplate(transactions).execute(status -> {
            try {
                logic.cancelForExecution("T", "instance", "deleted");
            } catch (ServiceException failure) {
                throw new RuntimeException(failure);
            }
            return null;
        });
    }

    private FmSystemTaskExecution row(String oid, String status, int generation) {
        var row = new FmSystemTaskExecution();
        row.setOid(oid);
        row.setTenantId("T");
        row.setProcessInstanceId("instance");
        row.setTaskType("GROOVY");
        row.setStatus(status);
        row.setGeneration(generation);
        return row;
    }
}
