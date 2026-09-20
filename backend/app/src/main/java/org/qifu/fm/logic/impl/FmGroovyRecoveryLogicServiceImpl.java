package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.logic.IFmGroovyRecoveryLogicService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW,
        rollbackFor = Exception.class, timeout = 30)
public class FmGroovyRecoveryLogicServiceImpl implements IFmGroovyRecoveryLogicService {

    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final Clock clock;

    @Autowired
    public FmGroovyRecoveryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts) {
        this(executions, attempts, Clock.systemUTC());
    }

    public FmGroovyRecoveryLogicServiceImpl(IFmSystemTaskExecutionService executions,
            IFmSystemTaskAttemptService attempts, Clock clock) {
        this.executions = executions;
        this.attempts = attempts;
        this.clock = clock;
    }

    @Override
    public boolean expire(String tenantId, String invocationId, int expectedGeneration) throws ServiceException {
        require(tenantId != null && !tenantId.isBlank() && invocationId != null
                && !invocationId.isBlank() && expectedGeneration > 0);
        var row = executions.lockInvocation(tenantId, invocationId);
        if (row == null) {
            return false;
        }
        require(tenantId.equals(row.getTenantId()) && invocationId.equals(row.getInvocationId())
                && "GROOVY".equals(row.getTaskType()));
        // Re-read time after obtaining the lock: another committer/reaper may have won the race.
        var now = Date.from(clock.instant());
        if (!"RUNNING".equals(row.getStatus()) || !Integer.valueOf(expectedGeneration).equals(row.getGeneration())
                || row.getLeaseUntil() == null || row.getLeaseUntil().after(now)) {
            return false;
        }
        require(row.getOid() != null && row.getStartedAt() != null && !row.getStartedAt().after(now)
                && row.getAttemptNo() != null && row.getAttemptNo() > 0
                && row.getCompletedAt() == null && row.getResultSha256() == null);
        var running = attempts.selectListByParams(Map.of("tenantId", tenantId,
                "executionOid", row.getOid(), "status", "RUNNING")).getValue();
        require(running != null && running.size() == 1 && running.getFirst() != null);
        var attempt = running.getFirst();
        require(tenantId.equals(attempt.getTenantId()) && row.getOid().equals(attempt.getExecutionOid())
                && "RUNNING".equals(attempt.getStatus()) && row.getGeneration().equals(attempt.getGeneration())
                && Objects.equals(row.getAttemptNo(), attempt.getAttemptNo())
                && row.getLeaseUntil().equals(attempt.getLeaseUntil()) && attempt.getCompletedAt() == null
                && attempt.getRequestId() != null && !attempt.getRequestId().isBlank()
                && attempt.getStartedAt() != null && !attempt.getStartedAt().before(row.getStartedAt())
                && !attempt.getStartedAt().after(now));
        require(attempts.finish(tenantId, row.getOid(), attempt.getRequestId(), expectedGeneration,
                "ABANDONED", now, "GROOVY_LEASE_EXPIRED") == 1);
        require(executions.expire(tenantId, row.getOid(), expectedGeneration, now) == 1);
        // Expiry is an unknown outcome, not evidence of a transient worker failure. Never re-arm here.
        return true;
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_RECOVERY_STATE_INVALID");
        }
    }
}
