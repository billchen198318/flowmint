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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeMapping.InputSnapshot;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService.LoadedInput;
import org.qifu.fm.logic.impl.FmGroovyRuntimeEvaluationLogicServiceImpl;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

class FmGroovyRuntimeEvaluationLogicTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final IFmSystemTaskAttemptService attempts = mock(IFmSystemTaskAttemptService.class);
    private final IFmGroovyRuntimeInputLogicService inputs = mock(IFmGroovyRuntimeInputLogicService.class);
    private final FmGroovyRuntimeRunner runner = mock(FmGroovyRuntimeRunner.class);
    private final FmSystemTaskExecution claim = new FmSystemTaskExecution();
    private final FmSystemTaskAttempt attempt = new FmSystemTaskAttempt();
    private final FmGroovyRuntimeEvaluationLogicServiceImpl logic = new FmGroovyRuntimeEvaluationLogicServiceImpl(
            executions, attempts, inputs, runner, new FmFormSubmissionValidator(JSON),
            Clock.fixed(GroovyRuntimeFixture.START.plusSeconds(1), ZoneOffset.UTC));
    private LoadedInput loaded;

    @BeforeEach
    void fixture() throws Exception {
        loaded = GroovyRuntimeFixture.loaded();
        claim.setOid("ledger");
        claim.setTenantId("T");
        claim.setInvocationId("invocation");
        claim.setProcessInstanceId("instance");
        claim.setProcessDefId("P");
        claim.setVersionNo(1);
        claim.setNodeId("calculate");
        claim.setFlowableExecutionId("execution");
        claim.setFirstJobId("first-job");
        claim.setTaskType("GROOVY");
        claim.setBindingSha256(loaded.preparation().bindingSha256());
        claim.setManifestSha256(loaded.preparation().manifestSha256());
        claim.setFormDataId("data");
        claim.setInputRevisionNo(3);
        claim.setExpectedFormLock(4);
        claim.setInputContent(loaded.snapshot().inputJson());
        claim.setContextContent(loaded.snapshot().contextJson());
        claim.setInputSha256(loaded.snapshot().sha256());
        claim.setStartedAt(Date.from(GroovyRuntimeFixture.START));
        claim.setStatus("RUNNING");
        claim.setGeneration(1);
        claim.setAttemptNo(1);
        claim.setLeaseUntil(Date.from(GroovyRuntimeFixture.START.plusSeconds(30)));
        attempt.setTenantId("T");
        attempt.setExecutionOid("ledger");
        attempt.setRequestId("attempt");
        attempt.setGeneration(1);
        attempt.setAttemptNo(1);
        attempt.setStatus("RUNNING");
        attempt.setStartedAt(claim.getStartedAt());
        attempt.setLeaseUntil(claim.getLeaseUntil());
        attempt.setEngineProfile(GroovyRuntimeFixture.PROFILE.canonical());
        when(executions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(claim)));
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt)));
        when(inputs.load("T", "execution", "invocation", GroovyRuntimeFixture.START, GroovyRuntimeFixture.PROFILE))
                .thenReturn(loaded);
        when(runner.profile()).thenReturn(GroovyRuntimeFixture.PROFILE);
        when(runner.execute(eq("T"), eq(GroovyRuntimeFixture.PROFILE), any(byte[].class), eq(3000)))
                .thenAnswer(call -> JSON.writeValueAsBytes(success(call.getArgument(2))));
    }

    @Test
    void buildsRuntimeWireFromFixedInputAndReturnsPlanWithoutWritingReceipt() throws Exception {
        when(runner.execute(eq("T"), eq(GroovyRuntimeFixture.PROFILE), any(byte[].class), eq(3000)))
                .thenAnswer(call -> {
                    assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
                    var request = JSON.readTree((byte[]) call.getArgument(2));
                    assertEquals("RUN", request.path("operation").asText());
                    assertEquals("RUNTIME", request.path("context").path("mode").asText());
                    assertEquals("applicant", request.path("context").path("applicantAccount").asText());
                    assertEquals("initiator", request.path("context").path("initiatorAccount").asText());
                    assertEquals("2026-09-12T00:00:00Z", request.path("context").path("startedAt").asText());
                    assertEquals("{\"amount\":12.5}", request.path("input").toString());
                    return JSON.writeValueAsBytes(success(call.getArgument(2)));
                });
        var result = evaluate();
        assertEquals(4, result.expectedFormLock());
        assertEquals(3, result.inputRevisionNo());
        assertTrue(result.plan().requiresWrite());
        assertEquals(25, JSON.readTree(result.plan().formJson()).path("total").asInt());
        assertEquals("RUNNING", claim.getStatus());
        verify(executions, never()).complete(any(), any(), anyInt(), any(), any(), any());
        verify(executions, never()).insert(any());
        verify(attempts, never()).insert(any());
    }

    @Test
    void rejectsActiveTransactionBeforeReadingLedgerOrStartingWorker() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            assertEquals("GROOVY_RUNTIME_TRANSACTION_ACTIVE",
                    assertThrows(ServiceException.class, this::evaluate).getMessage());
            verifyNoInteractions(executions, attempts, inputs, runner);
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void refusesCancelledExpiredOrForeignClaimBeforeWorker() {
        claim.setStatus("CANCELLED");
        assertThrows(ServiceException.class, this::evaluate);
        claim.setStatus("RUNNING");
        claim.setTenantId("OTHER");
        assertThrows(ServiceException.class, this::evaluate);
        claim.setTenantId("T");
        claim.setLeaseUntil(Date.from(GroovyRuntimeFixture.START.plusSeconds(1)));
        assertThrows(ServiceException.class, this::evaluate);
        verifyNoInteractions(inputs);
        verify(runner, never()).execute(any(), any(), any(), anyInt());
    }

    @Test
    void refusesForeignStaleOrAmbiguousAttempt() throws Exception {
        attempt.setRequestId("previous-attempt");
        assertThrows(ServiceException.class, this::evaluate);
        attempt.setRequestId("attempt");
        attempt.setGeneration(2);
        assertThrows(ServiceException.class, this::evaluate);
        attempt.setGeneration(1);
        when(attempts.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(attempt, attempt)));
        assertThrows(ServiceException.class, this::evaluate);
        verifyNoInteractions(inputs);
        verify(runner, never()).execute(any(), any(), any(), anyInt());
    }

    @Test
    void refusesChangedFormLockAndTamperedInputRatherThanRefreshingRetrySnapshot() {
        claim.setExpectedFormLock(2);
        assertThrows(ServiceException.class, this::evaluate);
        claim.setExpectedFormLock(4);
        claim.setInputContent("{\"amount\":999}");
        assertThrows(ServiceException.class, this::evaluate);
        verify(runner, never()).execute(any(), any(), any(), anyInt());
    }

    @Test
    void refusesLateResponseWhenClaimCancelledOrFormChangedDuringWorker() throws Exception {
        when(runner.execute(any(), any(), any(), anyInt())).thenAnswer(call -> {
            claim.setStatus("CANCELLED");
            return JSON.writeValueAsBytes(success(call.getArgument(2)));
        });
        assertThrows(ServiceException.class, this::evaluate);
        claim.setStatus("RUNNING");
        var changed = new LoadedInput("T", "execution", "instance", "P", 1, "calculate", "data", 5,
                loaded.savedFormJson(), loaded.snapshot(), loaded.preparation());
        doAnswer(call -> JSON.writeValueAsBytes(success(call.getArgument(2))))
                .when(runner).execute(any(), any(), any(), anyInt());
        when(inputs.load("T", "execution", "invocation", GroovyRuntimeFixture.START, GroovyRuntimeFixture.PROFILE))
                .thenReturn(loaded, changed);
        assertThrows(ServiceException.class, this::evaluate);
        verify(executions, never()).complete(any(), any(), anyInt(), any(), any(), any());
    }

    @Test
    void rejectsMismatchedResponseGenerationInvalidResultAndOversizedResponse() throws Exception {
        when(runner.execute(any(), any(), any(), anyInt())).thenAnswer(call -> {
            var response = success(call.getArgument(2));
            response.put("generation", 99);
            return JSON.writeValueAsBytes(response);
        });
        assertThrows(ServiceException.class, this::evaluate);
        doAnswer(call -> {
            var response = success(call.getArgument(2));
            response.putObject("result").put("total", "secret-wrong-type");
            return JSON.writeValueAsBytes(response);
        }).when(runner).execute(any(), any(), any(), anyInt());
        assertEquals("GROOVY_RUNTIME_CONTRACT_INVALID",
                assertThrows(ServiceException.class, this::evaluate).getMessage());
        doReturn(new byte[300 * 1024 + 1]).when(runner).execute(any(), any(), any(), anyInt());
        assertThrows(ServiceException.class, this::evaluate);
    }

    @Test
    void preservesSafeScriptFailureCodeAndDoesNotTreatFailureAsSuccess() throws Exception {
        when(runner.execute(any(), any(), any(), anyInt())).thenAnswer(call -> {
            var response = success(call.getArgument(2));
            response.remove(List.of("result", "logs", "logsTruncated"));
            response.put("status", "FAILED");
            response.put("errorCode", "GROOVY_RUNTIME_ERROR");
            return JSON.writeValueAsBytes(response);
        });
        assertEquals("GROOVY_RUNTIME_ERROR", assertThrows(ServiceException.class, this::evaluate).getMessage());
    }

    @Test
    void revokedOrUnreadableProfilePolicyDeniesEvaluationBeforeWorkerIo() throws Exception {
        for (String code : List.of("ENGINE_PROFILE_DISABLED", "GROOVY_PROFILE_POLICY_UNAVAILABLE")) {
            org.mockito.Mockito.doThrow(new ServiceException(code)).when(runner).profile();
            assertEquals(code, assertThrows(ServiceException.class, this::evaluate).getMessage());
        }
        verify(runner, never()).execute(any(), any(), any(), anyInt());
    }

    @Test
    void rejectsCorruptSnapshotAndWorkerIdentityLength() {
        var snapshot = loaded.snapshot();
        assertThrows(IllegalArgumentException.class, () -> loaded.preparation().runRequest(
                new InputSnapshot("{\"amount\":13}", snapshot.contextJson(), 3, snapshot.sha256()), "attempt", 1));
        assertThrows(IllegalArgumentException.class,
                () -> loaded.preparation().runRequest(snapshot, "x".repeat(65), 1));
    }

    private IFmGroovyRuntimeEvaluationLogicService.Evaluation evaluate() throws ServiceException {
        return logic.evaluate("T", "invocation", "attempt");
    }

    private ObjectNode success(byte[] bytes) {
        var request = JSON.readTree(bytes);
        var response = JSON.createObjectNode();
        for (String field : List.of("protocolVersion", "profile", "invocationId", "attemptId",
                "generation", "bindingSha256")) {
            response.set(field, request.get(field));
        }
        response.put("status", "SUCCEEDED");
        response.putObject("result").put("total", 25);
        response.putArray("logs").add("not for runtime audit");
        response.put("logsTruncated", false);
        return response;
    }
}
