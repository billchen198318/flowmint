package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.job.api.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.domain.workflow.FmGroovyExecutingJob;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService.Evaluation;
import org.qifu.fm.logic.impl.FmGroovyRuntimeCommitLogicServiceImpl;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyRuntimeCommitLogicTest {

    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final IFmFormDataService forms = mock(IFmFormDataService.class);
    private final IFmFormSnapshotService snapshots = mock(IFmFormSnapshotService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final FmGroovyFlowableTransactionGuard guard = mock(FmGroovyFlowableTransactionGuard.class);
    private final DelegateExecution engine = mock(DelegateExecution.class);
    private final FmGroovyExecutingJob executingJob = mock(FmGroovyExecutingJob.class);
    private final Job job = mock(Job.class);
    private final FmSystemTaskExecution row = new FmSystemTaskExecution();
    private final GroovyTransactionFixture transactions = new GroovyTransactionFixture();
    private IFmGroovyRuntimeCommitLogicService logic;
    private Evaluation evaluation;
    private String invocation;

    @BeforeEach
    void fixture() throws Exception {
        when(job.getTenantId()).thenReturn("T");
        when(job.getId()).thenReturn("job");
        when(job.getCorrelationId()).thenReturn("occurrence");
        when(job.getExecutionId()).thenReturn("execution");
        when(job.getProcessInstanceId()).thenReturn("instance");
        when(job.getProcessDefinitionId()).thenReturn("deployed:1");
        when(job.getElementId()).thenReturn("calculate");
        when(job.getJobType()).thenReturn("message");
        when(job.getJobHandlerType()).thenReturn("async-continuation");
        when(job.isExclusive()).thenReturn(true);
        invocation = new FmGroovyJobIdentity("T", "execution", "instance", "deployed:1", "calculate").invocation(job);
        when(executingJob.current()).thenReturn(job);
        var loaded = GroovyRuntimeFixture.loaded(invocation, GroovyRuntimeFixture.START);
        var validator = new FmFormSubmissionValidator(JsonMapper.builder().build());
        var plan = loaded.preparation().apply(loaded.savedFormJson(), "{\"total\":25}", validator);
        evaluation = new Evaluation("T", "ledger", invocation, "attempt", 1, "execution", "instance", "calculate",
                "data", 3, 4, loaded.snapshot().sha256(), loaded.preparation().bindingSha256(),
                loaded.preparation().manifestSha256(), "{\"total\":25}", plan);
        logic = transactions.proxy((IFmGroovyRuntimeCommitLogicService) new FmGroovyRuntimeCommitLogicServiceImpl(
                executions, attempts, inputs, forms, snapshots, runner, validator, guard, executingJob,
                Clock.fixed(GroovyRuntimeFixture.START.plusSeconds(1), ZoneOffset.UTC)));
        when(engine.getTenantId()).thenReturn("T");
        when(engine.getId()).thenReturn("execution");
        when(engine.getProcessInstanceId()).thenReturn("instance");
        when(engine.getProcessDefinitionId()).thenReturn("deployed:1");
        when(engine.getCurrentActivityId()).thenReturn("calculate");
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId(invocation);
        row.setStatus("RUNNING");
        row.setGeneration(1);
        row.setAttemptNo(1);
        row.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        row.setLeaseUntil(Date.from(GroovyRuntimeFixture.START.plusSeconds(60)));
        row.setFlowableExecutionId("execution");
        row.setProcessInstanceId("instance");
        row.setNodeId("calculate");
        row.setFormDataId("data");
        row.setInputRevisionNo(3);
        row.setExpectedFormLock(4);
        row.setInputSha256(evaluation.inputSha256());
        row.setBindingSha256(evaluation.bindingSha256());
        row.setManifestSha256(evaluation.manifestSha256());
        when(executions.lockInvocation("T", invocation)).thenReturn(row);
        var attempt = new FmSystemTaskAttempt();
        attempt.setTenantId("T");
        attempt.setExecutionOid("ledger");
        attempt.setRequestId("attempt");
        attempt.setGeneration(1);
        attempt.setAttemptNo(1);
        attempt.setStatus("RUNNING");
        attempt.setLeaseUntil(row.getLeaseUntil());
        attempt.setEngineProfile(GroovyRuntimeFixture.PROFILE.canonical());
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt)));
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(forms.lockByFormDataId("T", "data")).thenReturn("form-oid");
        when(inputs.load("T", "execution", invocation, GroovyRuntimeFixture.START, GroovyRuntimeFixture.PROFILE))
                .thenReturn(loaded);
        when(forms.updateDataContent("T", "data", plan.formJson(), 4)).thenReturn(1);
        var updated = new FmFormData();
        updated.setTenantId("T");
        updated.setFormDataId("data");
        updated.setFormVersionNo(1);
        updated.setRevisionNo(4);
        updated.setLockVersion(5);
        updated.setDataContent(plan.formJson());
        when(forms.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(updated)));
        when(snapshots.insertSystemTaskSnapshot(any())).thenAnswer(call -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            return "snapshot";
        });
        when(attempts.finish(eq("T"), eq("ledger"), eq("attempt"), eq(1), eq("SUCCEEDED"), any(), isNull())).thenReturn(1);
        when(executions.complete(eq("T"), eq("ledger"), eq(1), any(), eq(plan.resultSha256()), eq("snapshot"))).thenReturn(1);
    }

    @Test
    void joinsCallerTransactionAndWritesNextRevisionSnapshotReceiptAndEngineVariables() {
        apply();
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);
        var captured = ArgumentCaptor.forClass(FmFormSnapshot.class);
        try {
            verify(snapshots).insertSystemTaskSnapshot(captured.capture());
        } catch (ServiceException impossible) {
            throw new AssertionError(impossible);
        }
        assertEquals("SYSTEM_TASK_APPLY", captured.getValue().getActionType());
        assertEquals(4, captured.getValue().getRevisionNo());
        verify(engine).setVariable(eq(FmTaskAssignmentListener.VARIABLE_FORM_DATA), any());
    }

    @Test
    void mandatoryProxyCannotCreateAnIndependentSuccessTransaction() {
        assertThrows(IllegalTransactionStateException.class, () -> logic.apply(engine, evaluation));
        assertEquals(0, transactions.commits);
    }

    @Test
    void lateCancelledOrSupersededGenerationCannotWriteForm() {
        row.setStatus("CANCELLED");
        assertThrows(RuntimeException.class, this::apply);
        row.setStatus("RUNNING");
        row.setGeneration(2);
        assertThrows(RuntimeException.class, this::apply);
        verify(forms, never()).updateDataContent(any(), any(), any(), anyInt());
    }

    @Test
    void lostFormCasAndSnapshotFailureRollBackWithoutSuccessReceipt() throws Exception {
        when(forms.updateDataContent(any(), any(), any(), anyInt())).thenReturn(0);
        assertThrows(RuntimeException.class, this::apply);
        when(forms.updateDataContent(any(), any(), any(), anyInt())).thenReturn(1);
        doThrow(new ServiceException("SNAPSHOT_FAILURE")).when(snapshots).insertSystemTaskSnapshot(any());
        assertThrows(RuntimeException.class, this::apply);
        verify(executions, never()).complete(any(), any(), anyInt(), any(), any(), any());
        verify(engine, never()).setVariable(any(), any());
        assertEquals(2, transactions.rollbacks);
    }

    @Test
    void failedReceiptOrDownstreamFlowRollsBackSameTransaction() throws Exception {
        when(executions.complete(any(), any(), anyInt(), any(), any(), any())).thenReturn(0);
        assertThrows(RuntimeException.class, this::apply);
        verify(engine, never()).setVariable(any(), any());
        when(executions.complete(any(), any(), anyInt(), any(), any(), any())).thenReturn(1);
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).execute(status -> {
            try {
                logic.apply(engine, evaluation);
            } catch (ServiceException failure) {
                throw new RuntimeException(failure);
            }
            throw new IllegalStateException("Simulated next Flowable activity failure");
        }));
        assertEquals(0, transactions.commits);
        assertEquals(2, transactions.rollbacks);
    }

    @Test
    void modifiedPlanOrMismatchedEngineIdentityIsRejected() {
        when(engine.getId()).thenReturn("other-execution");
        assertThrows(RuntimeException.class, this::apply);
        when(engine.getId()).thenReturn("execution");
        evaluation = new Evaluation("T", "ledger", invocation, "attempt", 1, "execution", "instance", "calculate",
                "data", 3, 4, evaluation.inputSha256(), evaluation.bindingSha256(), evaluation.manifestSha256(),
                "{\"total\":999}", evaluation.plan());
        assertThrows(RuntimeException.class, this::apply);
        verify(forms, never()).updateDataContent(any(), any(), any(), anyInt());
    }

    @Test
    void missingOrNextLoopJobCannotApplyOldResult() throws Exception {
        when(executingJob.current()).thenReturn(null);
        assertThrows(RuntimeException.class, this::apply);
        when(executingJob.current()).thenReturn(job);
        when(job.getCorrelationId()).thenReturn("next-loop");
        assertThrows(RuntimeException.class, this::apply);
        verify(forms, never()).lockByFormDataId(any(), any());
        verify(executions, never()).complete(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void technicalJobMovePreservesOccurrenceAtCommit() {
        when(job.getId()).thenReturn("restored-job");
        apply();
        assertEquals(1, transactions.commits);
    }

    private void apply() {
        new TransactionTemplate(transactions).execute(status -> {
            try {
                logic.apply(engine, evaluation);
            } catch (ServiceException failure) {
                throw new RuntimeException(failure);
            }
            return null;
        });
    }
}
