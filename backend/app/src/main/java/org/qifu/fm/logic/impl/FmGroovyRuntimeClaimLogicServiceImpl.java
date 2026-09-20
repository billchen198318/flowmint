package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.flowable.common.engine.impl.context.Context;
import org.flowable.engine.ManagementService;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyRuntimeClaimLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService.LoadedInput;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW,
        rollbackFor = Exception.class, timeout = 30)
public class FmGroovyRuntimeClaimLogicServiceImpl implements IFmGroovyRuntimeClaimLogicService {

    private final ManagementService management;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final FmGroovyRuntimeRunner runner;
    private final Clock clock;
    private final IFmProcessInstanceService processes;

    @Autowired
    public FmGroovyRuntimeClaimLogicServiceImpl(ManagementService management,
            IFmGroovyRuntimeInputLogicService inputs, IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, FmGroovyRuntimeRunner runner, IFmProcessInstanceService processes) {
        this(management, inputs, executions, attempts, runner, processes, Clock.systemUTC());
    }

    public FmGroovyRuntimeClaimLogicServiceImpl(ManagementService management,
            IFmGroovyRuntimeInputLogicService inputs, IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, FmGroovyRuntimeRunner runner,
            IFmProcessInstanceService processes, Clock clock) {
        this.management = management;
        this.inputs = inputs;
        this.executions = executions;
        this.attempts = attempts;
        this.runner = runner;
        this.clock = clock;
        this.processes = processes;
    }

    @Override
    public Claim begin(FmGroovyJobIdentity identity) throws ServiceException {
        require(identity != null && Context.getCommandContext() == null);
        org.qifu.fm.domain.workflow.FmGroovyVersionManifest.EngineProfile profile;
        try {
            profile = runner.profile();
        } catch (ServiceException denied) {
            if (!"ENGINE_PROFILE_DISABLED".equals(denied.getMessage())
                    && !"GROOVY_PROFILE_POLICY_UNAVAILABLE".equals(denied.getMessage())) {
                throw denied;
            }
            // Persist the attempted admission so even the first revoked occurrence has an Incident.
            // Evaluation checks runner.profile() again before any worker IO and records the rejection.
            profile = runner.profileForFailureRecording();
        }
        var process = processes.lockInstance(identity.tenantId(), identity.processInstanceId());
        require(process != null && identity.tenantId().equals(process.getTenantId())
                && identity.processInstanceId().equals(process.getProcessInstanceId())
                && "RUNNING".equals(process.getInstanceStatus()));
        var jobs = management.createJobQuery().executionId(identity.executionId()).listPage(0, 2);
        require(jobs != null && jobs.size() == 1);
        var job = jobs.getFirst();
        String invocation = identity.invocation(job);
        var existing = executions.selectListByParams(
                Map.of("tenantId", identity.tenantId(), "invocationId", invocation)).getValue();
        require(existing != null && existing.size() <= 1);
        // datetime(3) round-trip must not change the input context/hash on the first read.
        var instant = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        var now = Date.from(instant);
        FmSystemTaskExecution row;
        if (existing.isEmpty()) {
            var loaded = inputs.load(identity.tenantId(), identity.executionId(), invocation, instant, profile);
            require(identity.processInstanceId().equals(loaded.processInstanceId())
                    && identity.nodeId().equals(loaded.nodeId()));
            row = initial(identity, job.getId(), invocation, loaded, now);
            row = executions.insert(row).getValueEmptyThrowMessage();
        } else {
            row = executions.lockInvocation(identity.tenantId(), invocation);
            require(row != null && identity.tenantId().equals(row.getTenantId())
                    && invocation.equals(row.getInvocationId()) && identity.executionId().equals(row.getFlowableExecutionId())
                    && identity.processInstanceId().equals(row.getProcessInstanceId()) && identity.nodeId().equals(row.getNodeId())
                    && row.getStartedAt() != null);
            // No automatic replay of RUNNING/FAILED/SUCCEEDED. Recovery must explicitly re-arm READY.
            require("READY".equals(row.getStatus()));
            // Evaluation checks the complete persisted snapshot after this attempt has committed.
            // A concurrent form change must produce a durable FAILED attempt, not strand READY.
        }
        require(row.getGeneration() != null && row.getGeneration() >= 0
                && row.getGeneration() < Integer.MAX_VALUE && row.getAttemptNo() != null
                && row.getAttemptNo() >= 0 && row.getAttemptNo() < 3);
        int generation = row.getGeneration() + 1;
        var leaseUntil = Date.from(instant.plusSeconds(60));
        require(executions.claim(identity.tenantId(), row.getOid(), row.getGeneration(), now, leaseUntil) == 1);
        String requestId = UUID.randomUUID().toString();
        var attempt = new FmSystemTaskAttempt();
        attempt.setTenantId(identity.tenantId());
        attempt.setExecutionOid(row.getOid());
        attempt.setAttemptNo(row.getAttemptNo() + 1);
        attempt.setGeneration(generation);
        attempt.setRequestId(requestId);
        attempt.setEngineProfile(profile.canonical());
        attempt.setStartedAt(now);
        attempt.setLeaseUntil(leaseUntil);
        attempt.setStatus("RUNNING");
        attempts.insert(attempt).getValueEmptyThrowMessage();
        return new Claim(identity.tenantId(), row.getOid(), invocation, requestId, generation);
    }

    @Override
    public void fail(Claim claim, String errorCode) throws ServiceException {
        require(claim != null);
        var now = Date.from(clock.instant());
        var row = executions.lockInvocation(claim.tenantId(), claim.invocationId());
        if (row == null || !claim.executionOid().equals(row.getOid())
                || !Integer.valueOf(claim.generation()).equals(row.getGeneration()) || !"RUNNING".equals(row.getStatus())) {
            return; // A completed, cancelled or superseded generation must not be overwritten by a late failure.
        }
        require(attempts.finish(claim.tenantId(), claim.executionOid(), claim.attemptId(), claim.generation(),
                "FAILED", now, errorCode) == 1);
        require(executions.fail(claim.tenantId(), claim.executionOid(), claim.generation(), now, errorCode) == 1);
    }

    private FmSystemTaskExecution initial(FmGroovyJobIdentity identity, String jobId, String invocation,
            LoadedInput loaded, Date now) {
        var row = new FmSystemTaskExecution();
        row.setTenantId(identity.tenantId());
        row.setInvocationId(invocation);
        row.setProcessInstanceId(loaded.processInstanceId());
        row.setProcessDefId(loaded.processDefId());
        row.setVersionNo(loaded.processVersionNo());
        row.setNodeId(loaded.nodeId());
        row.setFlowableExecutionId(identity.executionId());
        row.setFirstJobId(jobId);
        row.setTaskType("GROOVY");
        row.setBindingSha256(loaded.preparation().bindingSha256());
        row.setManifestSha256(loaded.preparation().manifestSha256());
        row.setFormDataId(loaded.formDataId());
        row.setInputRevisionNo(loaded.snapshot().revisionNo());
        row.setExpectedFormLock(loaded.expectedFormLock());
        row.setInputContent(loaded.snapshot().inputJson());
        row.setContextContent(loaded.snapshot().contextJson());
        row.setInputSha256(loaded.snapshot().sha256());
        row.setStartedAt(now);
        row.setStatus("READY");
        row.setGeneration(0);
        row.setAttemptNo(0);
        return row;
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RUNTIME_CLAIM_INVALID");
        }
    }
}
