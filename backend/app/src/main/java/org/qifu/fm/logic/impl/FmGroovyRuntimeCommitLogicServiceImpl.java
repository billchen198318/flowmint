package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.flowable.engine.delegate.DelegateExecution;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.domain.workflow.FmGroovyContractJson;
import org.qifu.fm.domain.workflow.FmGroovyExecutingJob;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmGroovyRuntimeCommitLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService.Evaluation;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.MANDATORY,
        rollbackFor = Exception.class)
public class FmGroovyRuntimeCommitLogicServiceImpl implements IFmGroovyRuntimeCommitLogicService {

    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build();
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final IFmFormDataService forms;
    private final IFmFormSnapshotService snapshots;
    private final FmGroovyRuntimeRunner runner;
    private final FmFormSubmissionValidator validator;
    private final FmGroovyFlowableTransactionGuard transactionGuard;
    private final Clock clock;
    private final FmGroovyExecutingJob executingJob;

    @Autowired
    public FmGroovyRuntimeCommitLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmGroovyRuntimeInputLogicService inputs, IFmFormDataService forms,
            IFmFormSnapshotService snapshots, FmGroovyRuntimeRunner runner, FmFormSubmissionValidator validator,
            FmGroovyFlowableTransactionGuard transactionGuard, FmGroovyExecutingJob executingJob) {
        this(executions, attempts, inputs, forms, snapshots, runner, validator, transactionGuard,
                executingJob, Clock.systemUTC());
    }

    public FmGroovyRuntimeCommitLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmGroovyRuntimeInputLogicService inputs, IFmFormDataService forms,
            IFmFormSnapshotService snapshots, FmGroovyRuntimeRunner runner, FmFormSubmissionValidator validator,
            FmGroovyFlowableTransactionGuard transactionGuard, FmGroovyExecutingJob executingJob, Clock clock) {
        this.executions = executions;
        this.attempts = attempts;
        this.inputs = inputs;
        this.forms = forms;
        this.snapshots = snapshots;
        this.runner = runner;
        this.validator = validator;
        this.transactionGuard = transactionGuard;
        this.clock = clock;
        this.executingJob = executingJob;
    }

    @Override
    public void apply(DelegateExecution execution, Evaluation evaluation) throws ServiceException {
        transactionGuard.require();
        require(execution != null && evaluation != null && evaluation.plan() != null
                && evaluation.tenantId().equals(execution.getTenantId())
                && evaluation.flowableExecutionId().equals(execution.getId())
                && evaluation.processInstanceId().equals(execution.getProcessInstanceId())
                && evaluation.nodeId().equals(execution.getCurrentActivityId()) && !execution.isEnded());
        var identity = new FmGroovyJobIdentity(execution.getTenantId(), execution.getId(),
                execution.getProcessInstanceId(), execution.getProcessDefinitionId(), execution.getCurrentActivityId());
        require(evaluation.invocationId().equals(identity.invocation(executingJob.current())));
        var now = Date.from(clock.instant());
        var row = executions.lockInvocation(evaluation.tenantId(), evaluation.invocationId());
        require(row != null && "RUNNING".equals(row.getStatus())
                && evaluation.tenantId().equals(row.getTenantId()) && evaluation.executionOid().equals(row.getOid())
                && evaluation.invocationId().equals(row.getInvocationId())
                && Integer.valueOf(evaluation.generation()).equals(row.getGeneration())
                && row.getLeaseUntil() != null && row.getLeaseUntil().after(now) && row.getStartedAt() != null
                && evaluation.flowableExecutionId().equals(row.getFlowableExecutionId())
                && evaluation.processInstanceId().equals(row.getProcessInstanceId()) && evaluation.nodeId().equals(row.getNodeId())
                && evaluation.formDataId().equals(row.getFormDataId())
                && Integer.valueOf(evaluation.inputRevisionNo()).equals(row.getInputRevisionNo())
                && Integer.valueOf(evaluation.expectedFormLock()).equals(row.getExpectedFormLock())
                && evaluation.inputSha256().equals(row.getInputSha256())
                && evaluation.bindingSha256().equals(row.getBindingSha256())
                && evaluation.manifestSha256().equals(row.getManifestSha256()));
        var profile = runner.profile();
        var runningAttempts = attempts.selectListByParams(Map.of("tenantId", evaluation.tenantId(),
                "executionOid", row.getOid(), "status", "RUNNING")).getValue();
        require(runningAttempts != null && runningAttempts.size() == 1);
        var attempt = runningAttempts.getFirst();
        require(attempt != null && evaluation.tenantId().equals(attempt.getTenantId())
                && row.getOid().equals(attempt.getExecutionOid()) && evaluation.attemptId().equals(attempt.getRequestId())
                && row.getGeneration().equals(attempt.getGeneration()) && row.getAttemptNo().equals(attempt.getAttemptNo())
                && "RUNNING".equals(attempt.getStatus()) && row.getLeaseUntil().equals(attempt.getLeaseUntil())
                && profile.canonical().equals(attempt.getEngineProfile()));
        require(forms.lockByFormDataId(evaluation.tenantId(), evaluation.formDataId()) != null);
        var loaded = inputs.load(evaluation.tenantId(), evaluation.flowableExecutionId(), evaluation.invocationId(),
                row.getStartedAt().toInstant(), profile);
        require(evaluation.formDataId().equals(loaded.formDataId())
                && evaluation.expectedFormLock() == loaded.expectedFormLock()
                && evaluation.inputRevisionNo() == loaded.snapshot().revisionNo()
                && evaluation.inputSha256().equals(loaded.snapshot().sha256())
                && evaluation.bindingSha256().equals(loaded.preparation().bindingSha256())
                && evaluation.manifestSha256().equals(loaded.preparation().manifestSha256()));
        try {
            var plan = loaded.preparation().apply(loaded.savedFormJson(), evaluation.resultJson(), validator);
            require(plan.equals(evaluation.plan()));
            String snapshotOid = null;
            if (plan.requiresWrite()) {
                require(forms.updateDataContent(evaluation.tenantId(), evaluation.formDataId(),
                        plan.formJson(), evaluation.expectedFormLock()) == 1);
                var values = forms.selectListByParams(Map.of("tenantId", evaluation.tenantId(),
                        "formDataId", evaluation.formDataId())).getValue();
                require(values != null && values.size() == 1);
                var form = values.getFirst();
                require(form != null && evaluation.tenantId().equals(form.getTenantId())
                        && evaluation.formDataId().equals(form.getFormDataId())
                        && Objects.equals(form.getRevisionNo(), Math.addExact(evaluation.inputRevisionNo(), 1))
                        && Objects.equals(form.getLockVersion(), Math.addExact(evaluation.expectedFormLock(), 1))
                        && plan.formJson().equals(form.getDataContent()));
                var snapshot = new FmFormSnapshot();
                snapshot.setTenantId(evaluation.tenantId());
                snapshot.setFormDataId(evaluation.formDataId());
                snapshot.setProcessInstanceId(evaluation.processInstanceId());
                snapshot.setActionType("SYSTEM_TASK_APPLY");
                snapshot.setFormVersionNo(form.getFormVersionNo());
                snapshot.setRevisionNo(form.getRevisionNo());
                snapshot.setDataContent(plan.formJson());
                snapshot.setContentSha256(FmGroovyContractJson.sha256(plan.formJson()));
                snapshot.setSnapshotDate(now);
                snapshotOid = snapshots.insertSystemTaskSnapshot(snapshot);
                require(snapshotOid != null && !snapshotOid.isBlank());
            }
            require(profile.equals(runner.profile()));
            // Recheck lease at the actual write boundary, after all mapping and form validation.
            now = Date.from(clock.instant());
            require(attempts.finish(evaluation.tenantId(), row.getOid(), evaluation.attemptId(), evaluation.generation(),
                    "SUCCEEDED", now, null) == 1);
            require(executions.complete(evaluation.tenantId(), row.getOid(), evaluation.generation(), now,
                    plan.resultSha256(), snapshotOid) == 1);
            execution.setVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA,
                    JSON.readValue(plan.formJson(), new TypeReference<Map<String, Object>>() { }));
            // FutureJavaDelegate.afterExecution returns to Flowable, which advances the token in this same transaction.
        } catch (IllegalArgumentException | ArithmeticException invalid) {
            throw new ServiceException("GROOVY_RUNTIME_COMMIT_INVALID");
        }
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RUNTIME_COMMIT_INVALID");
        }
    }
}
