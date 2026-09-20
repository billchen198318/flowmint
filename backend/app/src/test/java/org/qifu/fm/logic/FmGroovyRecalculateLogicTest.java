package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.dto.command.FmGroovyRecalculateCommand;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.impl.FmGroovyRecalculateLogicServiceImpl;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;

class FmGroovyRecalculateLogicTest {
    private final FmGroovyOperateAccess access = mock(FmGroovyOperateAccess.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskIncidentActionService actions = mock(IFmSystemTaskIncidentActionService.class);
    private final IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
    private final IFmFormDataService forms = mock(IFmFormDataService.class);
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final FmGroovyRetryJob jobs = mock(FmGroovyRetryJob.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final FmProcessInstance process = new FmProcessInstance();
    private final FmFormData form = new FmFormData();
    private final List<FmSystemTaskIncidentAction> history = new ArrayList<>();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyRecalculateLogicService logic;
    private MockedStatic<UserUtils> users;

    @BeforeEach
    void setup() throws Exception {
        users = mockStatic(UserUtils.class);
        users.when(UserUtils::getCurrentUser).thenReturn(new User("operator", "", "Y", List.of()));
        logic = transactions.proxy((IFmGroovyRecalculateLogicService) service(true));
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
        row.setGeneration(5);
        row.setAttemptNo(3);
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setCompletedAt(new Date());
        row.setFormDataId("data");
        row.setInputRevisionNo(2);
        row.setInputContent("old-input-evidence");
        row.setBindingSha256(loaded.preparation().bindingSha256());
        row.setManifestSha256(loaded.preparation().manifestSha256());
        process.setTenantId("T");
        process.setProcessInstanceId("instance");
        process.setInstanceStatus("RUNNING");
        process.setFlowableProcessDefId("definition");
        form.setTenantId("T");
        form.setFormDataId("data");
        form.setDataStatus("SUBMITTED");
        form.setRevisionNo(3);
        form.setLockVersion(4);
        form.setDataContent(GroovyRuntimeFixture.FORM_DATA);
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(row)));
        when(executions.lockInvocation("T", "invocation")).thenReturn(row);
        when(executions.insert(any())).thenAnswer(call -> value(call.getArgument(0)));
        when(processes.lockInstance("T", "instance")).thenReturn(process);
        when(processes.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(process)));
        when(forms.lockStateByFormDataId("T", "data")).thenReturn(form);
        when(inputs.load(any(), any(), any(), any(), any())).thenAnswer(call ->
                GroovyRuntimeFixture.loaded(call.getArgument(2), call.getArgument(3)));
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(jobs.recalculate(eq(row), eq("definition"), any())).thenReturn("new-job");
        when(actions.history("T", "ledger", 0, 1)).thenAnswer(call -> history.stream().limit(1).toList());
        when(actions.lockHistory(eq("T"), eq("ledger"), any())).thenAnswer(call -> {
            String request = call.getArgument(2);
            return history.stream().filter(a -> request == null || request.equals(a.getRequestId()))
                    .sorted((a, b) -> b.getActionNo().compareTo(a.getActionNo())).limit(1).toList();
        });
        when(actions.insert(any())).thenAnswer(call -> {
            FmSystemTaskIncidentAction action = call.getArgument(0);
            history.add(action);
            return value(action);
        });
    }

    @AfterEach
    void close() {
        users.close();
    }

    @Test
    void previewOnlyReportsCurrentRevisionAndDoesNotMutateOrExecute() throws Exception {
        var preview = logic.preview("T", "invocation").getValue();
        assertEquals(2, preview.previousFormRevision());
        assertEquals(3, preview.formRevision());
        assertEquals(GroovyRuntimeFixture.loaded().snapshot().sha256(), preview.inputSha256());
        verify(executions, never()).insert(any());
        verify(actions, never()).insert(any());
        verify(jobs, never()).recalculate(any(), any(), any());
    }

    @Test
    void confirmationCreatesNewSnapshotAndAuditsLinkWithoutOverwritingOriginalFailure() throws Exception {
        String invocation = logic.recalculate("T", command("r")).getValue();
        var capture = ArgumentCaptor.forClass(FmSystemTaskExecution.class);
        verify(executions).insert(capture.capture());
        var successor = capture.getValue();
        assertNotEquals("invocation", invocation);
        assertEquals(invocation, successor.getInvocationId());
        assertEquals("invocation", successor.getParentInvocationId());
        assertEquals(3, successor.getInputRevisionNo());
        assertEquals(0, successor.getAttemptNo());
        assertEquals(0, successor.getGeneration());
        assertEquals("READY", successor.getStatus());
        assertEquals("new-job", successor.getFirstJobId());
        assertNotEquals(row.getStartedAt(), successor.getStartedAt());
        assertNotEquals(GroovyRuntimeFixture.loaded().snapshot().sha256(), successor.getInputSha256());
        assertEquals(GroovyRuntimeFixture.loaded(invocation, successor.getStartedAt().toInstant()).snapshot().contextJson(),
                successor.getContextContent());
        var correlation = ArgumentCaptor.forClass(String.class);
        verify(jobs).recalculate(eq(row), eq("definition"), correlation.capture());
        assertEquals(invocation, FmGroovyJobIdentity.invocationForCorrelation("T", correlation.getValue()));
        assertEquals("FAILED", row.getStatus());
        assertEquals("old-input-evidence", row.getInputContent());
        assertEquals("RECALCULATE", history.getFirst().getActionType());
        assertEquals("IGNORED", history.getFirst().getToStatus());
        assertEquals("operator", history.getFirst().getActor());
        assertEquals(invocation, history.getFirst().getTargetInvocationId());
        verify(executions, never()).update(any());
    }

    @Test
    void exactReplayReturnsOriginalSuccessorEvenAfterProcessEnds() throws Exception {
        String invocation = logic.recalculate("T", command("r")).getValue();
        process.setInstanceStatus("COMPLETED");
        form.setRevisionNo(9);
        assertEquals(invocation, logic.recalculate("T", command("r")).getValue());
        verify(executions, times(1)).insert(any());
        verify(jobs, times(1)).recalculate(any(), any(), any());
    }

    @Test
    void reusedRequestCannotChangePreviewOrActor() throws Exception {
        logic.recalculate("T", command("r"));
        var c = command("r");
        assertThrows(ServiceException.class, () -> logic.recalculate("T", new FmGroovyRecalculateCommand(
                c.invocationId(), c.requestId(), c.expectedRevision(), c.expectedGeneration(), 4, c.formLock(),
                c.inputSha256(), c.bindingSha256(), c.reason())));
        users.when(UserUtils::getCurrentUser).thenReturn(new User("other", "", "Y", List.of()));
        assertThrows(ServiceException.class, () -> logic.recalculate("T", c));
        assertEquals(1, history.size());
    }

    @Test
    void changedFormAfterPreviewIncludingStaleSnapshotIsRejectedBeforeMovingJob() throws Exception {
        form.setRevisionNo(4);
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("revision")));
        form.setRevisionNo(3);
        form.setLockVersion(5);
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("lock")));
        form.setLockVersion(4);
        form.setDataContent("{\"amount\":99}");
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("snapshot")));
        verify(jobs, never()).recalculate(any(), any(), any());
    }

    @Test
    void unchangedFormAndChangedScriptDoNotBypassRetryBudgetOrPinnedVersion() throws Exception {
        row.setInputRevisionNo(3);
        assertThrows(ServiceException.class, () -> logic.preview("T", "invocation"));
        row.setInputRevisionNo(2);
        row.setBindingSha256("a".repeat(64));
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("version")));
        verify(jobs, never()).recalculate(any(), any(), any());
    }

    @Test
    void staleGenerationClosedIncidentAndCancellationPreventNewWork() throws Exception {
        row.setGeneration(6);
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("generation")));
        row.setGeneration(5);
        process.setInstanceStatus("CANCELLED");
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("cancelled")));
        process.setInstanceStatus("RUNNING");
        row.setStatus("RUNNING");
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("running")));
        verify(executions, never()).insert(any());
    }

    @Test
    void auditAndSuccessorInsertFailuresRollbackEngineMovement() throws Exception {
        doThrow(new ServiceException("audit failed")).when(actions).insert(any());
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("audit")));
        assertEquals(1, transactions.rollbacks);
        doThrow(new ServiceException("duplicate parent")).when(executions).insert(any());
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("duplicate")));
        assertEquals(2, transactions.rollbacks);
        assertEquals(0, transactions.commits);
    }

    @Test
    void engineFailureLeavesNoSuccessorOrAudit() throws Exception {
        doThrow(new ServiceException("engine failed")).when(jobs).recalculate(any(), any(), any());
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("engine")));
        verify(executions, never()).insert(any());
        verify(actions, never()).insert(any());
        assertEquals(1, transactions.rollbacks);
    }

    @Test
    void disabledUnauthorizedAndForeignTenantRequestsAreRejected() throws Exception {
        assertThrows(ServiceException.class, () -> service(false).preview("T", "invocation"));
        row.setTenantId("U");
        assertThrows(ServiceException.class, () -> logic.preview("T", "invocation"));
        doThrow(new ServiceException("denied")).when(access).require("T");
        assertThrows(ServiceException.class, () -> logic.recalculate("T", command("denied")));
        verify(jobs, never()).recalculate(any(), any(), any());
    }

    private FmGroovyRecalculateCommand command(String request) throws Exception {
        var loaded = GroovyRuntimeFixture.loaded();
        return new FmGroovyRecalculateCommand("invocation", request, 0, 5, 3, 4,
                loaded.snapshot().sha256(), loaded.preparation().bindingSha256(), "reviewed new form");
    }

    private FmGroovyRecalculateLogicServiceImpl service(boolean enabled) {
        return new FmGroovyRecalculateLogicServiceImpl(access, executions, actions, processes, forms, inputs, runner, jobs,
                enabled, true);
    }

    private static <T> DefaultResult<T> value(T value) {
        var result = new DefaultResult<T>();
        result.setValue(value);
        return result;
    }
}
