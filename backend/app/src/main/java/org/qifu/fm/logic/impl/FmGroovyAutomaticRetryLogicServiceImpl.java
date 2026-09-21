package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyAutomaticRetryPolicy;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.IFmGroovyAutomaticRetryLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Scheduler-only entry point; no worker IO and no operator impersonation. */
@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW,
        rollbackFor = Exception.class, timeout = 30)
public class FmGroovyAutomaticRetryLogicServiceImpl implements IFmGroovyAutomaticRetryLogicService {

    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final IFmSystemTaskIncidentActionService actions;
    private final IFmProcessInstanceService processes;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final FmGroovyRuntimeRunner runner;
    private final FmGroovyRetryJob jobs;
    private final Clock clock;

    @Autowired
    public FmGroovyAutomaticRetryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmSystemTaskIncidentActionService actions,
            IFmProcessInstanceService processes, IFmGroovyRuntimeInputLogicService inputs,
            FmGroovyRuntimeRunner runner, FmGroovyRetryJob jobs) {
        this(executions, attempts, actions, processes, inputs, runner, jobs, Clock.systemUTC());
    }

    public FmGroovyAutomaticRetryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, IFmSystemTaskIncidentActionService actions,
            IFmProcessInstanceService processes, IFmGroovyRuntimeInputLogicService inputs,
            FmGroovyRuntimeRunner runner, FmGroovyRetryJob jobs, Clock clock) {
        this.executions = executions;
        this.attempts = attempts;
        this.actions = actions;
        this.processes = processes;
        this.inputs = inputs;
        this.runner = runner;
        this.jobs = jobs;
        this.clock = clock;
    }

    @Override
    public boolean retry(String tenantId, String invocationId, int expectedGeneration) throws ServiceException {
        require(tenantId != null && !tenantId.isBlank() && invocationId != null
                && invocationId.matches("[A-Za-z0-9_-]{1,64}") && expectedGeneration > 0);
        var observed = executions.selectListByParams(Map.of("tenantId", tenantId, "invocationId", invocationId)).getValue();
        require(observed != null && observed.size() <= 1);
        if (observed.isEmpty()) {
            return false;
        }
        var before = observed.getFirst();
        require(before != null && tenantId.equals(before.getTenantId()) && invocationId.equals(before.getInvocationId())
                && "GROOVY".equals(before.getTaskType()) && before.getOid() != null && before.getProcessInstanceId() != null);
        // Serializes automatic retries with manual retry, handling, recalculation and cancellation.
        var process = processes.lockInstance(tenantId, before.getProcessInstanceId());
        require(process != null && tenantId.equals(process.getTenantId())
                && before.getProcessInstanceId().equals(process.getProcessInstanceId()));
        var row = executions.lockInvocation(tenantId, invocationId);
        if (row == null) {
            return false;
        }
        require(tenantId.equals(row.getTenantId()) && invocationId.equals(row.getInvocationId())
                && before.getOid().equals(row.getOid()) && before.getProcessInstanceId().equals(row.getProcessInstanceId()));
        var now = clock.instant();
        if (!"RUNNING".equals(process.getInstanceStatus()) || !Integer.valueOf(expectedGeneration).equals(row.getGeneration())
                || !FmGroovyAutomaticRetryPolicy.due(row, now)) {
            return false;
        }
        var history = actions.lockHistory(tenantId, row.getOid(), null);
        require(history != null && history.size() <= 1);
        int revision = 0;
        if (!history.isEmpty()) {
            var latest = history.getFirst();
            require(latest != null && tenantId.equals(latest.getTenantId()) && row.getOid().equals(latest.getExecutionOid())
                    && latest.getActionNo() != null && latest.getActionNo() > 0 && latest.getActionNo() < Integer.MAX_VALUE);
            if (!"OPEN".equals(latest.getToStatus())) {
                return false;
            }
            revision = latest.getActionNo();
        }
        var profile = runner.profile();
        // A real failed attempt must corroborate the ledger; READY recovery/lease expiry are not transient proof.
        var failed = attempts.selectListByParams(Map.of("tenantId", tenantId,
                "executionOid", row.getOid(), "status", "FAILED")).getValue();
        require(failed != null && !failed.isEmpty() && failed.size() <= 2);
        var matching = failed.stream().filter(attempt -> attempt != null
                && Objects.equals(row.getGeneration(), attempt.getGeneration())).toList();
        require(matching.size() == 1);
        var attempt = matching.getFirst();
        require(tenantId.equals(attempt.getTenantId()) && row.getOid().equals(attempt.getExecutionOid())
                && "FAILED".equals(attempt.getStatus()) && row.getAttemptNo().equals(attempt.getAttemptNo())
                && row.getErrorCode().equals(attempt.getErrorCode())
                && row.getCompletedAt().equals(attempt.getCompletedAt())
                && profile.canonical().equals(attempt.getEngineProfile()));
        checkInput(row, profile);
        // Also rejects missing, suspended, active or ambiguous occurrences. Wait for engine dead-letter commit.
        jobs.check(row, process.getFlowableProcessDefId());
        var action = new FmSystemTaskIncidentAction();
        action.setTenantId(tenantId);
        action.setExecutionOid(row.getOid());
        action.setRequestId(UUID.randomUUID().toString());
        action.setActionNo(revision + 1);
        action.setActionType("RETRY");
        action.setExpectedGeneration(expectedGeneration);
        action.setFromStatus("OPEN");
        action.setToStatus("OPEN");
        action.setActor("SYSTEM_TASK");
        action.setReason("自動退避重試：" + row.getErrorCode());
        action.setActionDate(Date.from(now));
        actions.insert(action).getValueEmptyThrowMessage();
        require(executions.rearm(tenantId, row.getOid(), expectedGeneration) == 1);
        jobs.restore(row, process.getFlowableProcessDefId());
        return true;
    }

    private void checkInput(FmSystemTaskExecution row, EngineProfile profile) throws ServiceException {
        require(row.getStartedAt() != null);
        var loaded = inputs.load(row.getTenantId(), row.getFlowableExecutionId(), row.getInvocationId(),
                row.getStartedAt().toInstant(), profile);
        require(Objects.equals(row.getTenantId(), loaded.tenantId())
                && Objects.equals(row.getFlowableExecutionId(), loaded.executionId())
                && Objects.equals(row.getProcessInstanceId(), loaded.processInstanceId())
                && Objects.equals(row.getProcessDefId(), loaded.processDefId())
                && Objects.equals(row.getVersionNo(), loaded.processVersionNo())
                && Objects.equals(row.getNodeId(), loaded.nodeId())
                && Objects.equals(row.getFormDataId(), loaded.formDataId())
                && Objects.equals(row.getInputRevisionNo(), loaded.snapshot().revisionNo())
                && Objects.equals(row.getExpectedFormLock(), loaded.expectedFormLock())
                && Objects.equals(row.getInputContent(), loaded.snapshot().inputJson())
                && Objects.equals(row.getContextContent(), loaded.snapshot().contextJson())
                && Objects.equals(row.getInputSha256(), loaded.snapshot().sha256())
                && Objects.equals(row.getBindingSha256(), loaded.preparation().bindingSha256())
                && Objects.equals(row.getManifestSha256(), loaded.preparation().manifestSha256()));
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_AUTOMATIC_RETRY_INVALID");
        }
    }
}
