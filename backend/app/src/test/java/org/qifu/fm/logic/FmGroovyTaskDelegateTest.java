package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;

import org.flowable.common.engine.api.async.AsyncTaskInvoker;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.flowable.FmGroovyTaskDelegate;
import org.qifu.fm.logic.IFmGroovyRuntimeClaimLogicService.Claim;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService.Evaluation;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

class FmGroovyTaskDelegateTest {

    @Test
    void futureNeverPassesEngineExecutionToWorkerAndCommitsOnOriginalThread() throws Exception {
        var claims = mock(IFmGroovyRuntimeClaimLogicService.class);
        var evaluator = mock(IFmGroovyRuntimeEvaluationLogicService.class);
        var commits = mock(IFmGroovyRuntimeCommitLogicService.class);
        var guard = mock(FmGroovyFlowableTransactionGuard.class);
        var delegate = new FmGroovyTaskDelegate(claims, evaluator, commits, guard);
        var engine = engine();
        var claim = new Claim("T", "ledger", "invocation", "attempt", 1);
        var evaluation = mock(Evaluation.class);
        long engineThread = Thread.currentThread().threadId();
        when(claims.begin(any())).thenAnswer(call -> {
            assertNotEquals(engineThread, Thread.currentThread().threadId());
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
            assertEquals(null, Context.getCommandContext());
            return claim;
        });
        when(evaluator.evaluate("T", "invocation", "attempt")).thenReturn(evaluation);
        doAnswer(call -> {
            assertEquals(engineThread, Thread.currentThread().threadId());
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            return null;
        }).when(commits).apply(engine, evaluation);
        try (var executor = Executors.newSingleThreadExecutor()) {
            AsyncTaskInvoker invoker = invoker(executor);
            var transactions = new GroovyTransactionFixture();
            new TransactionTemplate(transactions).execute(status -> {
                var outcome = delegate.execute(engine, invoker).join();
                delegate.afterExecution(engine, outcome);
                return null;
            });
            assertEquals(1, transactions.commits);
        }
        verify(commits).apply(engine, evaluation);
        verify(claims, never()).fail(any(), any());
    }

    @Test
    void scriptFailureRollsBackAndRecordsOnlySafeFailureInSeparateService() throws Exception {
        var claims = mock(IFmGroovyRuntimeClaimLogicService.class);
        var evaluator = mock(IFmGroovyRuntimeEvaluationLogicService.class);
        var commits = mock(IFmGroovyRuntimeCommitLogicService.class);
        var delegate = new FmGroovyTaskDelegate(claims, evaluator, commits, mock(FmGroovyFlowableTransactionGuard.class));
        var claim = new Claim("T", "ledger", "invocation", "attempt", 1);
        when(claims.begin(any())).thenReturn(claim);
        when(evaluator.evaluate(any(), any(), any())).thenThrow(new ServiceException("secret input and path"));
        var transactions = new GroovyTransactionFixture();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var failure = assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).execute(status -> {
                var engine = engine();
                delegate.afterExecution(engine, delegate.execute(engine, invoker(executor)).join());
                return null;
            }));
            assertEquals("GROOVY_RUNTIME_FAILED", failure.getMessage());
        }
        assertEquals(1, transactions.rollbacks);
        verify(claims).fail(claim, "GROOVY_RUNTIME_FAILED");
        verify(commits, never()).apply(any(), any());
    }

    @Test
    void trustedTransientFailuresReachDurableFailureRecording() throws Exception {
        for (String code : new String[] { "GROOVY_RUNTIME_BUSY", "GROOVY_RUNTIME_START_UNAVAILABLE" }) {
            var claims = mock(IFmGroovyRuntimeClaimLogicService.class);
            var evaluator = mock(IFmGroovyRuntimeEvaluationLogicService.class);
            var commits = mock(IFmGroovyRuntimeCommitLogicService.class);
            var delegate = new FmGroovyTaskDelegate(claims, evaluator, commits, mock(FmGroovyFlowableTransactionGuard.class));
            var claim = new Claim("T", "ledger", "invocation", "attempt", 1);
            when(claims.begin(any())).thenReturn(claim);
            when(evaluator.evaluate(any(), any(), any())).thenThrow(new ServiceException(code));
            try (var executor = Executors.newSingleThreadExecutor()) {
                var failure = assertThrows(IllegalStateException.class,
                        () -> new TransactionTemplate(new GroovyTransactionFixture()).execute(status -> {
                            var engine = engine();
                            delegate.afterExecution(engine, delegate.execute(engine, invoker(executor)).join());
                            return null;
                        }));
                assertEquals(code, failure.getMessage());
            }
            verify(claims).fail(claim, code);
            verify(commits, never()).apply(any(), any());
        }
    }

    @Test
    void revocationAtCommitBoundaryRecordsSpecificIncidentAfterRollback() throws Exception {
        var claims = mock(IFmGroovyRuntimeClaimLogicService.class);
        var evaluator = mock(IFmGroovyRuntimeEvaluationLogicService.class);
        var commits = mock(IFmGroovyRuntimeCommitLogicService.class);
        var delegate = new FmGroovyTaskDelegate(claims, evaluator, commits, mock(FmGroovyFlowableTransactionGuard.class));
        var claim = new Claim("T", "ledger", "invocation", "attempt", 1);
        when(claims.begin(any())).thenReturn(claim);
        when(evaluator.evaluate(any(), any(), any())).thenReturn(mock(Evaluation.class));
        org.mockito.Mockito.doThrow(new ServiceException("ENGINE_PROFILE_DISABLED")).when(commits).apply(any(), any());
        try (var executor = Executors.newSingleThreadExecutor()) {
            assertThrows(IllegalStateException.class, () -> new TransactionTemplate(new GroovyTransactionFixture()).execute(status -> {
                var engine = engine();
                delegate.afterExecution(engine, delegate.execute(engine, invoker(executor)).join());
                return null;
            }));
        }
        verify(claims).fail(claim, "ENGINE_PROFILE_DISABLED");
    }

    @Test
    void downstreamFailureAfterApplyRecordsRolledBackAttempt() throws Exception {
        var claims = mock(IFmGroovyRuntimeClaimLogicService.class);
        var evaluator = mock(IFmGroovyRuntimeEvaluationLogicService.class);
        var commits = mock(IFmGroovyRuntimeCommitLogicService.class);
        var delegate = new FmGroovyTaskDelegate(claims, evaluator, commits, mock(FmGroovyFlowableTransactionGuard.class));
        var claim = new Claim("T", "ledger", "invocation", "attempt", 1);
        when(claims.begin(any())).thenReturn(claim);
        when(evaluator.evaluate(any(), any(), any())).thenReturn(mock(Evaluation.class));
        try (var executor = Executors.newSingleThreadExecutor()) {
            assertThrows(IllegalStateException.class, () -> new TransactionTemplate(new GroovyTransactionFixture()).execute(status -> {
                var engine = engine();
                delegate.afterExecution(engine, delegate.execute(engine, invoker(executor)).join());
                throw new IllegalStateException("Downstream engine activity failed");
            }));
        }
        verify(claims).fail(eq(claim), eq("GROOVY_TRANSACTION_ROLLED_BACK"));
    }

    private static DelegateExecution engine() {
        var engine = mock(DelegateExecution.class);
        when(engine.getTenantId()).thenReturn("T");
        when(engine.getId()).thenReturn("execution");
        when(engine.getProcessInstanceId()).thenReturn("instance");
        when(engine.getProcessDefinitionId()).thenReturn("deployed:1");
        when(engine.getCurrentActivityId()).thenReturn("calculate");
        return engine;
    }

    private static AsyncTaskInvoker invoker(java.util.concurrent.Executor executor) {
        return new AsyncTaskInvoker() {
            @Override
            public <T> CompletableFuture<T> submit(Callable<T> task) {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        return task.call();
                    } catch (Exception failure) {
                        throw new CompletionException(failure);
                    }
                }, executor);
            }
        };
    }
}
