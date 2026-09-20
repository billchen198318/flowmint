package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.dto.command.FmGroovyRetryCommand;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.impl.FmGroovyRetryLogicServiceImpl;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;

class FmGroovyRetryLogicTest {
    private final FmGroovyOperateAccess access = mock(FmGroovyOperateAccess.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskIncidentActionService actions = mock(IFmSystemTaskIncidentActionService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final FmGroovyRetryJob jobs = mock(FmGroovyRetryJob.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final FmProcessInstance process = new FmProcessInstance();
    private final List<FmSystemTaskIncidentAction> history = new ArrayList<>();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyRetryLogicService logic;
    private MockedStatic<UserUtils> users;

    @BeforeEach
    void setup() throws Exception {
        users = mockStatic(UserUtils.class);
        users.when(UserUtils::getCurrentUser).thenReturn(new User("operator", "", "Y", List.of()));
        logic = transactions.proxy((IFmGroovyRetryLogicService) service(true));
        var loaded = GroovyRuntimeFixture.loaded();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId("invocation");
        row.setTaskType("GROOVY");
        row.setStatus("FAILED");
        row.setProcessInstanceId("instance");
        row.setProcessDefId("P");
        row.setVersionNo(1);
        row.setFlowableExecutionId("execution");
        row.setNodeId("calculate");
        row.setGeneration(1);
        row.setAttemptNo(1);
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setCompletedAt(new Date());
        row.setFormDataId(loaded.formDataId());
        row.setInputRevisionNo(loaded.snapshot().revisionNo());
        row.setExpectedFormLock(loaded.expectedFormLock());
        row.setInputContent(loaded.snapshot().inputJson());
        row.setContextContent(loaded.snapshot().contextJson());
        row.setInputSha256(loaded.snapshot().sha256());
        row.setBindingSha256(loaded.preparation().bindingSha256());
        row.setManifestSha256(loaded.preparation().manifestSha256());
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("RUNNING");
        process.setFlowableProcessDefId("definition");
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        when(executions.rearm("T", "ledger", 1)).thenReturn(1);
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(processes.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(process)));
        when(inputs.load(any(), any(), any(), any(), any())).thenReturn(loaded);
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(actions.history("T", "ledger", 0, 1)).thenAnswer(call -> history.stream().limit(1).toList());
        when(actions.lockHistory(eq("T"), eq("ledger"), any())).thenAnswer(call -> {
            String request = call.getArgument(2);
            return history.stream().filter(a -> request == null || request.equals(a.getRequestId()))
                    .sorted((a, b) -> b.getActionNo().compareTo(a.getActionNo())).limit(1).toList();
        });
        when(actions.insert(any())).thenAnswer(call -> {
            FmSystemTaskIncidentAction action = call.getArgument(0);
            history.add(action);
            var result = new DefaultResult<FmSystemTaskIncidentAction>();
            result.setValue(action);
            return result;
        });
    }

    @AfterEach
    void close() {
        users.close();
    }

    @Test
    void previewIsReadOnlyAndAcceptanceAuditsOriginalInput() throws Exception {
        var preview = logic.preview("T", "invocation").getValue();
        assertEquals(3, preview.inputRevision());
        assertEquals(2, preview.remainingAttempts());
        verify(actions, never()).insert(any());
        verify(jobs, never()).restore(any(), any());
        logic.retry("T", command("request", 0, 1));
        verify(jobs).restore(row, "definition");
        verify(executions).rearm("T", "ledger", 1);
        assertEquals("RETRY", history.getFirst().getActionType());
        assertEquals("operator", history.getFirst().getActor());
        assertEquals("reviewed", history.getFirst().getReason());
        assertEquals("OPEN", history.getFirst().getToStatus());
    }

    @Test
    void recoveredPreStartFailureCanRetryWithoutConsumingAPhantomAttempt() throws Exception {
        row.setAttemptNo(0);
        row.setErrorCode("GROOVY_START_FAILED");
        assertEquals(3, logic.preview("T", "invocation").getValue().remainingAttempts());
        logic.retry("T", command("request", 0, 1));
        verify(executions).rearm("T", "ledger", 1);
        verify(jobs).restore(row, "definition");
        assertEquals(0, row.getAttemptNo());
    }

    @Test
    void replayAfterCompletionDoesNotScheduleAgainAndConflictingReplayIsDenied() throws Exception {
        logic.retry("T", command("request", 0, 1));
        row.setStatus("SUCCEEDED");
        process.setInstanceStatus("COMPLETED");
        logic.retry("T", command("request", 0, 1));
        verify(jobs, times(1)).restore(any(), any());
        assertThrows(ServiceException.class, () -> logic.retry("T", command("request", 0, 2)));
        assertThrows(ServiceException.class, () -> logic.retry("T",
                new FmGroovyRetryCommand("invocation", "request", 0, 1, "different")));
        users.when(UserUtils::getCurrentUser).thenReturn(new User("another", "", "Y", List.of()));
        assertThrows(ServiceException.class, () -> logic.retry("T", command("request", 0, 1)));
    }

    @Test
    void staleRevisionGenerationAndCancelledProcessNeverSchedule() throws Exception {
        assertThrows(ServiceException.class, () -> logic.retry("T", command("stale", 1, 1)));
        assertThrows(ServiceException.class, () -> logic.retry("T", command("stale", 0, 2)));
        process.setInstanceStatus("CANCELLED");
        assertThrows(ServiceException.class, () -> logic.retry("T", command("cancelled", 0, 1)));
        verify(actions, never()).insert(any());
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void changedFormOrVersionCannotBecomeAnImplicitRecalculation() throws Exception {
        row.setInputSha256("b".repeat(64));
        assertThrows(ServiceException.class, () -> logic.retry("T", command("changed", 0, 1)));
        row.setInputSha256(GroovyRuntimeFixture.loaded().snapshot().sha256());
        row.setBindingSha256("c".repeat(64));
        assertThrows(ServiceException.class, () -> logic.preview("T", "invocation"));
        verify(actions, never()).insert(any());
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void budgetReceiptAndNonFailedStatesAreRejected() throws Exception {
        row.setAttemptNo(3);
        assertThrows(ServiceException.class, () -> logic.retry("T", command("budget", 0, 1)));
        row.setAttemptNo(2);
        row.setResultSha256("a".repeat(64));
        assertThrows(ServiceException.class, () -> logic.retry("T", command("receipt", 0, 1)));
        row.setResultSha256(null);
        row.setStatus("READY");
        assertThrows(ServiceException.class, () -> logic.retry("T", command("ready", 0, 1)));
        verify(jobs, never()).restore(any(), any());
    }

    @Test
    void auditFailureLostCasAndEngineFailureRollBackAcceptance() throws Exception {
        doThrow(new ServiceException("audit unavailable")).when(actions).insert(any());
        assertThrows(ServiceException.class, () -> logic.retry("T", command("audit", 0, 1)));
        verify(executions, never()).rearm(any(), any(), eq(1));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test
    void engineFailureRollsBackAndLostCasDoesNotMoveJob() throws Exception {
        when(executions.rearm("T", "ledger", 1)).thenReturn(0);
        assertThrows(ServiceException.class, () -> logic.retry("T", command("cas", 0, 1)));
        verify(jobs, never()).restore(any(), any());
        history.clear(); // fixture doesn't emulate database rollback
        when(executions.rearm("T", "ledger", 1)).thenReturn(1);
        doThrow(new ServiceException("engine unavailable")).when(jobs).restore(any(), any());
        assertThrows(ServiceException.class, () -> logic.retry("T", command("engine", 0, 1)));
        assertEquals(2, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test
    void disabledUnauthorizedForeignTenantAndBlankReasonAreDenied() throws Exception {
        assertThrows(ServiceException.class, () -> service(false).preview("T", "invocation"));
        assertThrows(ServiceException.class, () -> logic.retry("T", new FmGroovyRetryCommand("invocation", "r", 0, 1, " ")));
        row.setTenantId("U");
        assertThrows(ServiceException.class, () -> logic.preview("T", "invocation"));
        doThrow(new ServiceException("denied")).when(access).require("T");
        assertThrows(ServiceException.class, () -> logic.retry("T", command("denied", 0, 1)));
        verify(actions, never()).insert(any());
        verify(jobs, never()).restore(any(), any());
    }

    private FmGroovyRetryLogicServiceImpl service(boolean enabled) {
        return new FmGroovyRetryLogicServiceImpl(access, executions, actions, processes, inputs, runner, jobs, enabled, true);
    }

    private FmGroovyRetryCommand command(String id, int revision, int generation) {
        return new FmGroovyRetryCommand("invocation", id, revision, generation, " reviewed ");
    }
}
