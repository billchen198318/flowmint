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
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.impl.FmGroovyRecoveryLogicServiceImpl;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

class FmGroovyRecoveryLogicTest {

    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final FmSystemTaskAttempt attempt = new FmSystemTaskAttempt();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyRecoveryLogicService logic;
    private final Date now = Date.from(GroovyRuntimeFixture.START.plusSeconds(60));

    @BeforeEach
    void fixture() throws Exception {
        logic = transactions.proxy((IFmGroovyRecoveryLogicService) new FmGroovyRecoveryLogicServiceImpl(
                executions, attempts, Clock.fixed(now.toInstant(), ZoneOffset.UTC)));
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId("invocation");
        row.setTaskType("GROOVY");
        row.setStatus("RUNNING");
        row.setGeneration(1);
        row.setAttemptNo(1);
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setLeaseUntil(now);
        attempt.setTenantId("T");
        attempt.setExecutionOid("ledger");
        attempt.setRequestId("request");
        attempt.setStatus("RUNNING");
        attempt.setGeneration(1);
        attempt.setAttemptNo(1);
        attempt.setStartedAt(row.getStartedAt());
        attempt.setLeaseUntil(now);
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt)));
        when(attempts.finish("T", "ledger", "request", 1, "ABANDONED", now, "GROOVY_LEASE_EXPIRED"))
                .thenReturn(1);
        when(executions.expire("T", "ledger", 1, now)).thenReturn(1);
    }

    @Test
    void expirationAtLeaseBoundaryCommitsAttemptAndExecutionTogether() throws Exception {
        assertTrue(logic.expire("T", "invocation", 1));
        verify(attempts).finish("T", "ledger", "request", 1, "ABANDONED", now, "GROOVY_LEASE_EXPIRED");
        verify(executions).expire("T", "ledger", 1, now);
        verify(executions, never()).claim(any(), any(), anyInt(), any(), any());
        assertEquals(1, transactions.commits);
    }

    @Test
    void completedCancelledNewGenerationAndLiveLeaseAreNoOps() throws Exception {
        for (String status : List.of("SUCCEEDED", "CANCELLED", "FAILED", "READY")) {
            row.setStatus(status);
            assertFalse(logic.expire("T", "invocation", 1));
        }
        row.setStatus("RUNNING");
        row.setGeneration(2);
        assertFalse(logic.expire("T", "invocation", 1));
        row.setGeneration(1);
        row.setLeaseUntil(new Date(now.getTime() + 1));
        assertFalse(logic.expire("T", "invocation", 1));
        verify(attempts, never()).finish(any(), any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void lostExecutionUpdateRollsBackAttemptTransition() throws Exception {
        when(executions.expire("T", "ledger", 1, now)).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.expire("T", "invocation", 1));
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
    }

    @Test
    void wrongTenantAndAmbiguousOrSupersededAttemptFailClosed() throws Exception {
        row.setTenantId("OTHER");
        assertThrows(ServiceException.class, () -> logic.expire("T", "invocation", 1));
        row.setTenantId("T");
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt, attempt)));
        assertThrows(ServiceException.class, () -> logic.expire("T", "invocation", 1));
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt)));
        attempt.setGeneration(2);
        assertThrows(ServiceException.class, () -> logic.expire("T", "invocation", 1));
        verify(executions, never()).expire(any(), any(), anyInt(), any());
    }

    @Test
    void failedAttemptTransitionCannotExpireExecution() throws Exception {
        when(attempts.finish(any(), any(), any(), anyInt(), eq("ABANDONED"), any(), any())).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.expire("T", "invocation", 1));
        verify(executions, never()).expire(any(), any(), anyInt(), any());
        assertEquals(1, transactions.rollbacks);
    }
}
