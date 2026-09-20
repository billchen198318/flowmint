package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.qifu.fm.dto.command.FmGroovyIncidentHandleCommand;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.impl.FmGroovyIncidentHandlingLogicServiceImpl;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;

class FmGroovyIncidentHandlingLogicTest {

    private final FmGroovyOperateAccess access = mock(FmGroovyOperateAccess.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskIncidentActionService actions = mock(IFmSystemTaskIncidentActionService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final FmProcessInstance process = new FmProcessInstance();
    private final FmSystemTaskExecution execution = new FmSystemTaskExecution();
    private final List<FmSystemTaskIncidentAction> history = new ArrayList<>();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyIncidentHandlingLogicService logic;
    private MockedStatic<UserUtils> users;

    @BeforeEach
    void setup() throws Exception {
        users = mockStatic(UserUtils.class);
        users.when(UserUtils::getCurrentUser).thenReturn(new User("operator", "", "Y", List.of()));
        logic = transactions.proxy((IFmGroovyIncidentHandlingLogicService) new FmGroovyIncidentHandlingLogicServiceImpl(
                access, executions, actions, processes, true));
        process.setTenantId("T");
        process.setProcessInstanceId("P");
        process.setInstanceStatus("RUNNING");
        execution.setOid("E");
        execution.setTenantId("T");
        execution.setProcessInstanceId("P");
        execution.setInvocationId("I");
        execution.setTaskType("GROOVY");
        execution.setStatus("FAILED");
        when(executions.findIncidents("T", "I", 0, 2)).thenReturn(List.of(execution));
        when(executions.lockInvocation("T", "I")).thenReturn(execution);
        when(processes.lockInstance("T", "P")).thenReturn(process);
        when(actions.lockHistory(eq("T"), eq("E"), any())).thenAnswer(call -> {
            String requestId = call.getArgument(2);
            return history.stream().filter(value -> requestId == null || requestId.equals(value.getRequestId()))
                    .sorted((left, right) -> right.getActionNo().compareTo(left.getActionNo())).limit(1).toList();
        });
        when(actions.history(eq("T"), eq("E"), anyInt(), anyInt())).thenAnswer(call -> history.stream()
                .sorted((left, right) -> right.getActionNo().compareTo(left.getActionNo()))
                .skip((int) call.getArgument(2)).limit((int) call.getArgument(3)).toList());
        when(actions.insert(any())).thenAnswer(call -> {
            FmSystemTaskIncidentAction value = call.getArgument(0);
            history.add(value);
            var result = new DefaultResult<FmSystemTaskIncidentAction>();
            result.setValue(value);
            return result;
        });
    }

    @AfterEach
    void close() {
        users.close();
    }

    @Test
    void startsOpenAndAppendsActorReasonAndTransitionsWithoutChangingExecution() throws Exception {
        assertEquals("OPEN", logic.load("T", "I", 0).getValue().status());
        var handled = logic.handle("T", command("request-1", 0, "IGNORED")).getValue();
        assertEquals("IGNORED", handled.status());
        assertEquals("operator", handled.history().getFirst().actor());
        assertEquals("reviewed", handled.history().getFirst().reason());
        logic.handle("T", command("request-2", 1, "OPEN"));
        assertEquals(2, logic.load("T", "I", 0).getValue().history().size());
        assertEquals("FAILED", execution.getStatus());
        verify(executions, never()).update(any());
    }

    @Test
    void retryHistoryRemainsReadableDuringExecutionAndSuccessfulTaskCanBeResolved() throws Exception {
        execution.setIncidentOid("incident");
        execution.setStatus("RUNNING");
        assertEquals("OPEN", logic.load("T", "I", 0).getValue().status());
        assertThrows(ServiceException.class, () -> logic.handle("T", command("early", 0, "IGNORED")));
        execution.setStatus("SUCCEEDED");
        assertEquals("RESOLVED", logic.handle("T", command("done", 0, "RESOLVED")).getValue().status());
        assertEquals("SUCCEEDED", execution.getStatus());
    }

    @Test
    void statusRequestCannotReplayARetryAudit() throws Exception {
        var retry = new FmSystemTaskIncidentAction();
        retry.setTenantId("T");
        retry.setExecutionOid("E");
        retry.setRequestId("retry");
        retry.setActionType("RETRY");
        retry.setExpectedGeneration(1);
        retry.setActionNo(1);
        retry.setFromStatus("OPEN");
        retry.setToStatus("OPEN");
        retry.setActor("operator");
        retry.setReason("reviewed");
        history.add(retry);
        assertThrows(ServiceException.class, () -> logic.handle("T", command("retry", 0, "OPEN")));
        assertEquals("RETRY", logic.load("T", "I", 0).getValue().history().getFirst().actionType());
    }

    @Test
    void exactReplayDoesNotAppendAndRequestIdCannotBeReusedWithDifferentContent() throws Exception {
        var command = command("request", 0, "IGNORED");
        logic.handle("T", command);
        logic.handle("T", command);
        assertEquals(1, history.size());
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "RESOLVED")));
    }

    @Test
    void staleRevisionAndDirectClosedToClosedTransitionAreRejected() throws Exception {
        logic.handle("T", command("first", 0, "IGNORED"));
        assertThrows(ServiceException.class, () -> logic.handle("T", command("stale", 0, "OPEN")));
        assertThrows(ServiceException.class, () -> logic.handle("T", command("invalid", 1, "RESOLVED")));
        assertEquals(1, history.size());
    }

    @Test
    void resolutionRequiresTerminalProcessAndCanBeReopened() throws Exception {
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "RESOLVED")));
        process.setInstanceStatus("TERMINATED");
        assertEquals("RESOLVED", logic.handle("T", command("request", 0, "RESOLVED")).getValue().status());
        assertEquals("OPEN", logic.handle("T", command("reopen", 1, "OPEN")).getValue().status());
    }

    @Test
    void permissionAndFeatureGateRejectBeforeReadingOrWritingHistory() throws Exception {
        var disabled = new FmGroovyIncidentHandlingLogicServiceImpl(access, executions, actions, processes, false);
        assertThrows(ServiceException.class, () -> disabled.load("T", "I", 0));
        doThrow(new ServiceException("DENIED")).when(access).require("T");
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "IGNORED")));
        verify(actions, never()).insert(any());
        verify(executions, never()).findIncidents(any(), any(), anyInt(), anyInt());
    }

    @Test
    void auditInsertFailureRollsBackTheHandlingTransaction() throws Exception {
        doThrow(new ServiceException("AUDIT_FAILED")).when(actions).insert(any());
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "IGNORED")));
        assertEquals(0, transactions.commits);
        assertEquals(1, transactions.rollbacks);
    }

    @Test
    void blankReasonForeignExecutionAndChangedRuntimeStateCannotBeHandled() throws Exception {
        assertThrows(ServiceException.class, () -> logic.handle("T",
                new FmGroovyIncidentHandleCommand("I", "request", 0, "IGNORED", " ")));
        execution.setTenantId("OTHER");
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "IGNORED")));
        execution.setTenantId("T");
        execution.setStatus("RUNNING");
        assertThrows(ServiceException.class, () -> logic.handle("T", command("request", 0, "IGNORED")));
        verify(actions, never()).insert(any());
    }

    private FmGroovyIncidentHandleCommand command(String requestId, int revision, String target) {
        return new FmGroovyIncidentHandleCommand("I", requestId, revision, target, " reviewed ");
    }
}
