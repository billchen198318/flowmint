package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.domain.workflow.FmGroovyPreviewProtocol;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService.LoadedInput;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import tools.jackson.databind.json.JsonMapper;

/** No writes and no open transaction during worker IO. This is not a Delegate or a commit coordinator. */
@Service
@Transactional(propagation = Propagation.NEVER)
public class FmGroovyRuntimeEvaluationLogicServiceImpl implements IFmGroovyRuntimeEvaluationLogicService {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final FmGroovyRuntimeRunner runner;
    private final FmFormSubmissionValidator validator;
    private final Clock clock;

    @Autowired
    public FmGroovyRuntimeEvaluationLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmGroovyRuntimeInputLogicService inputs,
            FmGroovyRuntimeRunner runner, FmFormSubmissionValidator validator) {
        this(executions, attempts, inputs, runner, validator, Clock.systemUTC());
    }

    public FmGroovyRuntimeEvaluationLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmGroovyRuntimeInputLogicService inputs,
            FmGroovyRuntimeRunner runner, FmFormSubmissionValidator validator, Clock clock) {
        this.executions = executions;
        this.attempts = attempts;
        this.inputs = inputs;
        this.runner = runner;
        this.validator = validator;
        this.clock = clock;
    }

    @Override
    public Evaluation evaluate(String tenantId, String invocationId, String attemptId) throws ServiceException {
        require(text(tenantId) && wireId(invocationId) && wireId(attemptId));
        if (TransactionSynchronizationManager.isActualTransactionActive()
                || org.flowable.common.engine.impl.context.Context.getCommandContext() != null) {
            throw new ServiceException("GROOVY_RUNTIME_TRANSACTION_ACTIVE");
        }
        var profile = runner.profile();
        var claim = claim(tenantId, invocationId, attemptId, profile);
        var loaded = load(claim, profile);
        try {
            var request = loaded.preparation().runRequest(loaded.snapshot(), attemptId, claim.getGeneration());
            byte[] bytes = JSON.writeValueAsBytes(request);
            if (bytes.length > 768 * 1024) {
                throw new ServiceException("GROOVY_RUNTIME_REQUEST_INVALID");
            }
            // The persisted input was compared above; its canonical wire content must be identical on retry.
            byte[] response = runner.execute(tenantId, profile, bytes, loaded.preparation().timeoutMs());
            var verified = FmGroovyPreviewProtocol.response(response, request,
                    claim.getManifestSha256(), null, 0);
            require(profile.equals(runner.profile()));
            var current = claim(tenantId, invocationId, attemptId, profile);
            require(sameClaim(claim, current));
            var reloaded = load(current, profile);
            require(loaded.savedFormJson().equals(reloaded.savedFormJson()));
            if (!"SUCCEEDED".equals(verified.status())) {
                throw new ServiceException(verified.errorCode());
            }
            var plan = loaded.preparation().apply(loaded.savedFormJson(), verified.result().toString(), validator);
            return new Evaluation(tenantId, claim.getOid(), invocationId, attemptId, claim.getGeneration(),
                    claim.getFlowableExecutionId(), claim.getProcessInstanceId(), claim.getNodeId(),
                    claim.getFormDataId(), claim.getInputRevisionNo(), claim.getExpectedFormLock(),
                    claim.getInputSha256(), claim.getBindingSha256(), claim.getManifestSha256(),
                    verified.result().toString(), plan);
        } catch (IllegalArgumentException invalid) {
            throw new ServiceException("GROOVY_RUNTIME_CONTRACT_INVALID");
        }
    }

    private FmSystemTaskExecution claim(String tenantId, String invocationId, String attemptId,
            EngineProfile profile) throws ServiceException {
        var claim = one(executions.selectListByParams(
                Map.of("tenantId", tenantId, "invocationId", invocationId)).getValue());
        require(tenantId.equals(claim.getTenantId()) && invocationId.equals(claim.getInvocationId())
                && text(claim.getOid()) && text(claim.getFlowableExecutionId()) && text(claim.getFirstJobId())
                && "GROOVY".equals(claim.getTaskType()) && "RUNNING".equals(claim.getStatus())
                && claim.getGeneration() != null && claim.getGeneration() > 0
                && claim.getAttemptNo() != null && claim.getAttemptNo() > 0
                && claim.getStartedAt() != null && !claim.getStartedAt().toInstant().isAfter(clock.instant())
                && claim.getLeaseUntil() != null && claim.getLeaseUntil().toInstant().isAfter(clock.instant())
                && claim.getCompletedAt() == null && claim.getResultSha256() == null);
        // The query is bounded by the unique execution and RUNNING status; reject multiple active attempts.
        var attempt = one(attempts.selectListByParams(Map.of("tenantId", tenantId,
                "executionOid", claim.getOid(), "status", "RUNNING")).getValue());
        require(tenantId.equals(attempt.getTenantId()) && claim.getOid().equals(attempt.getExecutionOid())
                && attemptId.equals(attempt.getRequestId()) && "RUNNING".equals(attempt.getStatus())
                && claim.getGeneration().equals(attempt.getGeneration())
                && claim.getAttemptNo().equals(attempt.getAttemptNo())
                && profile.canonical().equals(attempt.getEngineProfile())
                && Objects.equals(claim.getLeaseUntil(), attempt.getLeaseUntil())
                && attempt.getStartedAt() != null && !attempt.getStartedAt().before(claim.getStartedAt())
                && !attempt.getStartedAt().toInstant().isAfter(clock.instant()) && attempt.getCompletedAt() == null);
        return claim;
    }

    private LoadedInput load(FmSystemTaskExecution claim, EngineProfile profile) throws ServiceException {
        var loaded = inputs.load(claim.getTenantId(), claim.getFlowableExecutionId(), claim.getInvocationId(),
                claim.getStartedAt().toInstant(), profile);
        require(claim.getTenantId().equals(loaded.tenantId())
                && claim.getFlowableExecutionId().equals(loaded.executionId())
                && Objects.equals(claim.getProcessInstanceId(), loaded.processInstanceId())
                && Objects.equals(claim.getProcessDefId(), loaded.processDefId())
                && Objects.equals(claim.getVersionNo(), loaded.processVersionNo())
                && Objects.equals(claim.getNodeId(), loaded.nodeId())
                && Objects.equals(claim.getFormDataId(), loaded.formDataId())
                && Objects.equals(claim.getInputRevisionNo(), loaded.snapshot().revisionNo())
                && Objects.equals(claim.getExpectedFormLock(), loaded.expectedFormLock())
                && Objects.equals(claim.getInputContent(), loaded.snapshot().inputJson())
                && Objects.equals(claim.getContextContent(), loaded.snapshot().contextJson())
                && Objects.equals(claim.getInputSha256(), loaded.snapshot().sha256())
                && Objects.equals(claim.getBindingSha256(), loaded.preparation().bindingSha256())
                && Objects.equals(claim.getManifestSha256(), loaded.preparation().manifestSha256()));
        return loaded;
    }

    private static boolean sameClaim(FmSystemTaskExecution before, FmSystemTaskExecution after) {
        return before.getOid().equals(after.getOid()) && before.getGeneration().equals(after.getGeneration())
                && before.getAttemptNo().equals(after.getAttemptNo())
                && Objects.equals(before.getInputSha256(), after.getInputSha256())
                && Objects.equals(before.getExpectedFormLock(), after.getExpectedFormLock())
                && Objects.equals(before.getBindingSha256(), after.getBindingSha256())
                && Objects.equals(before.getManifestSha256(), after.getManifestSha256())
                && Objects.equals(before.getFlowableExecutionId(), after.getFlowableExecutionId())
                && Objects.equals(before.getProcessInstanceId(), after.getProcessInstanceId())
                && Objects.equals(before.getProcessDefId(), after.getProcessDefId())
                && Objects.equals(before.getVersionNo(), after.getVersionNo())
                && Objects.equals(before.getNodeId(), after.getNodeId())
                && Objects.equals(before.getFormDataId(), after.getFormDataId())
                && Objects.equals(before.getInputRevisionNo(), after.getInputRevisionNo())
                && Objects.equals(before.getStartedAt(), after.getStartedAt())
                && Objects.equals(before.getLeaseUntil(), after.getLeaseUntil())
                && Objects.equals(before.getFirstJobId(), after.getFirstJobId());
    }

    private static boolean wireId(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}");
    }

    private static boolean text(String value) {
        return value != null && !value.isBlank() && value.length() <= 100;
    }

    private static <T> T one(List<T> values) throws ServiceException {
        require(values != null && values.size() == 1 && values.getFirst() != null);
        return values.getFirst();
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RUNTIME_CLAIM_INVALID");
        }
    }
}
