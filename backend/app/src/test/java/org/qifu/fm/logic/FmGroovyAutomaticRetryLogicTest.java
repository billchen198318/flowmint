package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.ArgumentCaptor;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.impl.FmGroovyAutomaticRetryLogicServiceImpl;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;

class FmGroovyAutomaticRetryLogicTest {

    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final IFmSystemTaskIncidentActionService actions = mock(IFmSystemTaskIncidentActionService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final FmGroovyRetryJob jobs = mock(FmGroovyRetryJob.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final FmSystemTaskAttempt attempt = new FmSystemTaskAttempt();
    private final FmProcessInstance process = new FmProcessInstance();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private final Date now = Date.from(GroovyRuntimeFixture.START.plusSeconds(60));
    private IFmGroovyAutomaticRetryLogicService logic;

    @BeforeEach
    void setup() throws Exception {
        logic = transactions.proxy((IFmGroovyAutomaticRetryLogicService) new FmGroovyAutomaticRetryLogicServiceImpl(
                executions, attempts, actions, processes, inputs, runner, jobs,
                Clock.fixed(now.toInstant(), ZoneOffset.UTC)));
        var loaded = GroovyRuntimeFixture.loaded();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId("invocation");
        row.setTaskType("GROOVY");
        row.setStatus("FAILED");
        row.setErrorCode("GROOVY_RUNTIME_BUSY");
        row.setProcessInstanceId("instance");
        row.setProcessDefId("P");
        row.setVersionNo(1);
        row.setFlowableExecutionId("execution");
        row.setNodeId("calculate");
        row.setGeneration(1);
        row.setAttemptNo(1);
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setCompletedAt(new Date(now.getTime() - 1000));
        row.setFormDataId(loaded.formDataId());
        row.setInputRevisionNo(loaded.snapshot().revisionNo());
        row.setExpectedFormLock(loaded.expectedFormLock());
        row.setInputContent(loaded.snapshot().inputJson());
        row.setContextContent(loaded.snapshot().contextJson());
        row.setInputSha256(loaded.snapshot().sha256());
        row.setBindingSha256(loaded.preparation().bindingSha256());
        row.setManifestSha256(loaded.preparation().manifestSha256());
        attempt.setTenantId("T");
        attempt.setExecutionOid("ledger");
        attempt.setStatus("FAILED");
        attempt.setGeneration(1);
        attempt.setAttemptNo(1);
        attempt.setCompletedAt(row.getCompletedAt());
        attempt.setErrorCode(row.getErrorCode());
        attempt.setEngineProfile(GroovyRuntimeFixture.PROFILE.canonical());
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("RUNNING");
        process.setFlowableProcessDefId("definition");
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        when(executions.rearm("T", "ledger", 1)).thenReturn(1);
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(inputs.load(any(), any(), any(), any(), any())).thenReturn(loaded);
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt)));
        when(actions.lockHistory("T", "ledger", null)).thenReturn(List.of());
        when(actions.insert(any())).thenAnswer(call -> {
            var result = new org.qifu.base.model.DefaultResult<FmSystemTaskIncidentAction>();
            result.setValue(call.getArgument(0));
            return result;
        });
    }

    @Test
    void dueFailureAuditsSystemActorAndRestoresSameOccurrenceInOneTransaction() throws Exception {
        assertTrue(logic.retry("T", "invocation", 1));
        var capture = ArgumentCaptor.forClass(FmSystemTaskIncidentAction.class);
        verify(actions).insert(capture.capture());
        assertEquals("SYSTEM_TASK", capture.getValue().getActor());
        assertEquals("RETRY", capture.getValue().getActionType());
        assertTrue(capture.getValue().getRequestId().matches("[a-f0-9-]{36}"));
        assertEquals(1, capture.getValue().getExpectedGeneration());
        assertEquals(now, capture.getValue().getActionDate());
        verify(jobs).restore(row, "definition");
        assertEquals(1, transactions.commits);
        assertEquals(1, row.getAttemptNo()); // No attempt is fabricated during acceptance.
    }

    @Test
    void firstAndSecondRetryCannotRunBeforeBackoffBoundary() throws Exception {
        row.setCompletedAt(new Date(now.getTime() - 999));
        assertFalse(logic.retry("T", "invocation", 1));
        row.setAttemptNo(2);
        row.setCompletedAt(new Date(now.getTime() - 4999));
        assertFalse(logic.retry("T", "invocation", 1));
        row.setCompletedAt(new Date(now.getTime() - 5000));
        attempt.setCompletedAt(row.getCompletedAt());
        attempt.setAttemptNo(2);
        assertTrue(logic.retry("T", "invocation", 1));
    }

    @Test
    void exhaustedBudgetAndUnknownOrPermanentFailuresNeverRetry() throws Exception {
        for (int count : new int[] { 0, 3, 4 }) {
            row.setAttemptNo(count);
            assertFalse(logic.retry("T", "invocation", 1));
        }
        row.setAttemptNo(1);
        for (String code : new String[] { "GROOVY_TIMEOUT", "GROOVY_MEMORY_LIMIT", "GROOVY_RUNTIME_ERROR",
                "GROOVY_RUNTIME_RUNNER_FAILED", "GROOVY_LEASE_EXPIRED", "ENGINE_PROFILE_DISABLED",
                "GROOVY_INPUT_INVALID", "GROOVY_OUTPUT_INVALID", "GROOVY_START_FAILED" }) {
            row.setErrorCode(code);
            assertFalse(logic.retry("T", "invocation", 1));
        }
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void staleScanAndCancelledOrCompletedWorkCannotRearm() throws Exception {
        assertFalse(logic.retry("T", "invocation", 2));
        for (String status : List.of("READY", "RUNNING", "CANCELLED", "SUCCEEDED")) {
            row.setStatus(status);
            assertFalse(logic.retry("T", "invocation", 1));
        }
        row.setStatus("FAILED");
        process.setInstanceStatus("CANCELLED");
        assertFalse(logic.retry("T", "invocation", 1));
        verify(actions, never()).insert(any());
    }

    @Test
    void operatorClosedIncidentSuppressesAutomaticRetry() throws Exception {
        var action = new FmSystemTaskIncidentAction();
        action.setTenantId("T");
        action.setExecutionOid("ledger");
        action.setActionNo(1);
        action.setToStatus("IGNORED");
        when(actions.lockHistory("T", "ledger", null)).thenReturn(List.of(action));
        assertFalse(logic.retry("T", "invocation", 1));
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void changedInputAndUnavailableProfileNeverSchedule() throws Exception {
        row.setInputSha256("changed");
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        when(runner.profile()).thenThrow(new ServiceException("ENGINE_PROFILE_DISABLED"));
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        verify(actions, never()).insert(any());
    }

    @Test
    void missingOrContradictoryAttemptAndForeignTenantFailClosed() throws Exception {
        attempt.setErrorCode("GROOVY_TIMEOUT");
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        attempt.setErrorCode(row.getErrorCode());
        attempt.setTenantId("OTHER");
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of()));
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void lostCasRollsBackAuditAndDoesNotRestoreJob() throws Exception {
        when(executions.rearm("T", "ledger", 1)).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void missingDeadLetterCannotConsumeBudgetOrWriteAudit() throws Exception {
        doThrow(new ServiceException("GROOVY_RETRY_JOB_INVALID")).when(jobs).check(any(), any());
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        verify(actions, never()).insert(any());
        verify(jobs, never()).restore(any(), any());
        assertEquals(1, row.getAttemptNo());
    }

    @Test
    void secondSchedulerCannotAcceptAlreadyRearmedGeneration() throws Exception {
        when(executions.rearm("T", "ledger", 1)).thenAnswer(call -> {
            row.setGeneration(2);
            row.setStatus("READY");
            return 1;
        });
        assertTrue(logic.retry("T", "invocation", 1));
        assertFalse(logic.retry("T", "invocation", 1));
        verify(jobs).restore(row, "definition");
        verify(actions).insert(any());
    }

    @Test
    void jobMoveFailureRollsBackAcceptance() throws Exception {
        doThrow(new ServiceException("GROOVY_RETRY_JOB_INVALID")).when(jobs).restore(any(), any());
        assertThrows(ServiceException.class, () -> logic.retry("T", "invocation", 1));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }
}
