package org.qifu.fm.flowable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.flowable.common.engine.api.async.AsyncTaskInvoker;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.FutureJavaDelegate;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.logic.IFmGroovyRuntimeClaimLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeClaimLogicService.Claim;
import org.qifu.fm.logic.IFmGroovyRuntimeCommitLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Worker IO uses a fresh thread; only afterExecution mutates the engine's transaction-bound execution. */
@Component("fmGroovyTaskDelegate")
public class FmGroovyTaskDelegate implements FutureJavaDelegate<FmGroovyTaskDelegate.Outcome> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FmGroovyTaskDelegate.class);
    private static final Set<String> SAFE_ERRORS = Set.of("GROOVY_RUNTIME_UNAVAILABLE", "ENGINE_PROFILE_DISABLED",
            "GROOVY_PROFILE_POLICY_UNAVAILABLE",
            "GROOVY_RUNTIME_BUSY", "GROOVY_RUNTIME_START_UNAVAILABLE",
            "GROOVY_RUNTIME_RUNNER_FAILED", "GROOVY_RUNTIME_TRANSACTION_ACTIVE", "GROOVY_RUNTIME_CLAIM_INVALID",
            "GROOVY_RUNTIME_INPUT_INVALID", "GROOVY_RUNTIME_IDENTITY_INVALID", "GROOVY_RUNTIME_DEFINITION_INVALID",
            "GROOVY_RUNTIME_CONTRACT_INVALID", "GROOVY_RUNTIME_COMMIT_INVALID", "GROOVY_JOB_IDENTITY_INVALID",
            "GROOVY_COMPILE_ERROR", "GROOVY_POLICY_DENIED", "GROOVY_INPUT_INVALID", "GROOVY_OUTPUT_INVALID",
            "GROOVY_RUNTIME_ERROR", "GROOVY_RUNTIME_REQUEST_INVALID", "GROOVY_TIMEOUT", "GROOVY_MEMORY_LIMIT");
    private final IFmGroovyRuntimeClaimLogicService claims;
    private final IFmGroovyRuntimeEvaluationLogicService evaluator;
    private final IFmGroovyRuntimeCommitLogicService commits;
    private final FmGroovyFlowableTransactionGuard transactionGuard;

    public FmGroovyTaskDelegate(IFmGroovyRuntimeClaimLogicService claims,
            IFmGroovyRuntimeEvaluationLogicService evaluator, IFmGroovyRuntimeCommitLogicService commits,
            FmGroovyFlowableTransactionGuard transactionGuard) {
        this.claims = claims;
        this.evaluator = evaluator;
        this.commits = commits;
        this.transactionGuard = transactionGuard;
    }

    @Override
    public CompletableFuture<Outcome> execute(DelegateExecution execution, AsyncTaskInvoker taskInvoker) {
        try {
            transactionGuard.require();
            var identity = new FmGroovyJobIdentity(execution.getTenantId(), execution.getId(),
                    execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentActivityId());
            var state = new InvocationState();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    state.transactionStatus.set(status);
                    if (status != STATUS_COMMITTED) {
                        recordFailure(state);
                    }
                }
            });
            return taskInvoker.submit(() -> {
                try {
                    if (Context.getCommandContext() != null
                            || TransactionSynchronizationManager.isActualTransactionActive()) {
                        throw new ServiceException("GROOVY_RUNTIME_TRANSACTION_ACTIVE");
                    }
                    Claim claim = claims.begin(identity);
                    state.claim.set(claim);
                    if (state.transactionStatus.get() != -1) {
                        recordFailure(state);
                        return new Outcome(null, "GROOVY_TRANSACTION_ROLLED_BACK");
                    }
                    Evaluation result = evaluator.evaluate(claim.tenantId(), claim.invocationId(), claim.attemptId());
                    return new Outcome(result, null, state);
                } catch (Exception failure) {
                    String code = safeCode(failure);
                    state.errorCode.set(code);
                    return new Outcome(null, code);
                } finally {
                    if (state.transactionStatus.get() != -1
                            && state.transactionStatus.get() != TransactionSynchronization.STATUS_COMMITTED) {
                        recordFailure(state);
                    }
                }
            });
        } catch (Exception failure) {
            throw new IllegalStateException("GROOVY_RUNTIME_START_FAILED");
        }
    }

    @Override
    public void afterExecution(DelegateExecution execution, Outcome outcome) {
        if (outcome == null || outcome.errorCode() != null || outcome.evaluation() == null) {
            throw new IllegalStateException(outcome == null ? "GROOVY_RUNTIME_FAILED" : outcome.errorCode());
        }
        try {
            commits.apply(execution, outcome.evaluation());
        } catch (Exception failure) {
            String code = safeCode(failure);
            if (outcome.state != null) {
                outcome.state.errorCode.set(code);
            }
            throw new IllegalStateException(code);
        }
    }

    private void recordFailure(InvocationState state) {
        Claim claim = state.claim.get();
        if (claim == null) {
            return;
        }
        try {
            // REQUIRES_NEW: failure survives the engine transaction rollback. Generation guards make this idempotent.
            claims.fail(claim, state.errorCode.get());
        } catch (Exception failure) {
            // An unavailable database leaves a RUNNING lease for the recovery reaper; never log worker details.
            LOGGER.error("GROOVY_FAILURE_RECORDING_FAILED invocation={}", claim.invocationId());
        }
    }

    private static String safeCode(Exception failure) {
        return failure instanceof ServiceException && SAFE_ERRORS.contains(failure.getMessage())
                ? failure.getMessage() : "GROOVY_RUNTIME_FAILED";
    }

    private static final class InvocationState {
        private final AtomicReference<Claim> claim = new AtomicReference<>();
        private final AtomicReference<String> errorCode = new AtomicReference<>("GROOVY_TRANSACTION_ROLLED_BACK");
        private final AtomicInteger transactionStatus = new AtomicInteger(-1);
    }

    public static final class Outcome {

        private final Evaluation evaluation;
        private final String errorCode;
        private final InvocationState state;

        public Outcome(Evaluation evaluation, String errorCode) {
            this(evaluation, errorCode, null);
        }

        private Outcome(Evaluation evaluation, String errorCode, InvocationState state) {
            this.evaluation = evaluation;
            this.errorCode = errorCode;
            this.state = state;
        }

        public Evaluation evaluation() {
            return evaluation;
        }

        public String errorCode() {
            return errorCode;
        }
    }
}
