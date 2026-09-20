package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyReadyJobProbe;
import org.qifu.fm.domain.workflow.FmGroovyReadyJobProbe.State;
import org.qifu.fm.logic.IFmGroovyReadyRecoveryLogicService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW,
        rollbackFor = Exception.class, timeout = 30)
public class FmGroovyReadyRecoveryLogicServiceImpl implements IFmGroovyReadyRecoveryLogicService {
    private final IFmSystemTaskExecutionService executions;
    private final IFmProcessInstanceService processes;
    private final FmGroovyReadyJobProbe jobs;
    private final Clock clock;

    @Autowired
    public FmGroovyReadyRecoveryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmProcessInstanceService processes, FmGroovyReadyJobProbe jobs) {
        this(executions, processes, jobs, Clock.systemUTC());
    }

    public FmGroovyReadyRecoveryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmProcessInstanceService processes, FmGroovyReadyJobProbe jobs, Clock clock) {
        this.executions = executions;
        this.processes = processes;
        this.jobs = jobs;
        this.clock = clock;
    }

    @Override
    public boolean recover(String tenantId, String invocationId, int expectedGeneration) throws ServiceException {
        require(tenantId != null && !tenantId.isBlank() && invocationId != null
                && invocationId.matches("[A-Za-z0-9_-]{1,64}") && expectedGeneration >= 0);
        var observed = executions.selectListByParams(Map.of("tenantId", tenantId, "invocationId", invocationId)).getValue();
        require(observed != null && observed.size() <= 1);
        if (observed.isEmpty()) return false;
        var before = observed.getFirst();
        require(before != null && tenantId.equals(before.getTenantId()) && invocationId.equals(before.getInvocationId())
                && "GROOVY".equals(before.getTaskType()) && before.getProcessInstanceId() != null && before.getOid() != null);
        // Same lock order as claim, cancellation, retry and recalculation.
        var process = processes.lockInstance(tenantId, before.getProcessInstanceId());
        require(process != null && tenantId.equals(process.getTenantId())
                && before.getProcessInstanceId().equals(process.getProcessInstanceId()));
        var row = executions.lockInvocation(tenantId, invocationId);
        if (row == null) return false;
        require(tenantId.equals(row.getTenantId()) && invocationId.equals(row.getInvocationId())
                && "GROOVY".equals(row.getTaskType()) && before.getOid().equals(row.getOid())
                && before.getProcessInstanceId().equals(row.getProcessInstanceId()));
        var instant = clock.instant();
        var now = Date.from(instant);
        var cutoff = Date.from(instant.minusSeconds(GRACE_SECONDS));
        var readyAt = row.getNextAttemptAt() == null ? row.getStartedAt() : row.getNextAttemptAt();
        if (!"READY".equals(row.getStatus()) || !Integer.valueOf(expectedGeneration).equals(row.getGeneration())
                || readyAt == null || readyAt.after(cutoff)) return false;
        require(expectedGeneration < Integer.MAX_VALUE && row.getAttemptNo() != null && row.getAttemptNo() >= 0
                && row.getCompletedAt() == null && row.getResultSha256() == null
                && row.getFormSnapshotOid() == null && row.getLeaseUntil() == null);
        String status;
        String code;
        if (process.getInstanceStatus() != null
                && Set.of("COMPLETED", "CANCELLED", "TERMINATED", "REJECTED").contains(process.getInstanceStatus())) {
            status = "CANCELLED";
            code = "GROOVY_PROCESS_ENDED_BEFORE_START";
        } else {
            require("RUNNING".equals(process.getInstanceStatus()));
            State state = jobs.inspect(row, process.getFlowableProcessDefId());
            require(state != null);
            if (state == State.WAITING) return false;
            status = state == State.EXECUTION_REMOVED ? "CANCELLED" : "FAILED";
            code = switch (state) {
                case DEAD_LETTER -> "GROOVY_START_FAILED";
                case MISSING -> "GROOVY_JOB_MISSING";
                case EXECUTION_REMOVED -> "GROOVY_EXECUTION_REMOVED";
                default -> throw new IllegalStateException("GROOVY_READY_RECOVERY_STATE_INVALID");
            };
        }
        require(executions.finishReady(tenantId, row.getOid(), expectedGeneration, cutoff, now, status, code) == 1);
        return true;
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) throw new ServiceException("GROOVY_READY_RECOVERY_STATE_INVALID");
    }
}
