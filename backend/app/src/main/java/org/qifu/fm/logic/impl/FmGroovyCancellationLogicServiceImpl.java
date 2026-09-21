package org.qifu.fm.logic.impl;

import java.time.Clock;
import java.util.Date;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.logic.IFmGroovyCancellationLogicService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManager", propagation = Propagation.MANDATORY,
        rollbackFor = Exception.class)
public class FmGroovyCancellationLogicServiceImpl implements IFmGroovyCancellationLogicService {

    private final IFmProcessInstanceService processes;
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskAttemptService attempts;
    private final boolean enabled;
    private final Clock clock;

    @Autowired
    public FmGroovyCancellationLogicServiceImpl(IFmProcessInstanceService processes,
            IFmSystemTaskExecutionService executions, IFmSystemTaskAttemptService attempts,
            @Value("${flowmint.groovy.runtime.enabled:false}") boolean runtimeEnabled,
            @Value("${flowmint.groovy.recovery.enabled:false}") boolean recoveryEnabled) {
        this(processes, executions, attempts, runtimeEnabled || recoveryEnabled, Clock.systemUTC());
    }

    public FmGroovyCancellationLogicServiceImpl(IFmProcessInstanceService processes,
            IFmSystemTaskExecutionService executions, IFmSystemTaskAttemptService attempts, boolean enabled, Clock clock) {
        this.processes = processes;
        this.executions = executions;
        this.attempts = attempts;
        this.enabled = enabled;
        this.clock = clock;
    }

    @Override
    public void cancelForProcess(String tenantId, String processInstanceId) throws ServiceException {
        // Existing non-Groovy installations have not deployed the runtime ledger tables.
        if (!enabled) {
            return;
        }
        var process = processes.lockInstance(tenantId, processInstanceId);
        require(process != null && tenantId.equals(process.getTenantId())
                && processInstanceId.equals(process.getProcessInstanceId()) && process.getInstanceStatus() != null
                && Set.of("CANCELLED", "TERMINATED", "REJECTED").contains(process.getInstanceStatus()));
        cancelActive(tenantId, processInstanceId, null);
    }

    @Override
    public void cancelForExecution(String tenantId, String processInstanceId, String executionId)
            throws ServiceException {
        if (!enabled) {
            return;
        }
        require(tenantId != null && !tenantId.isBlank() && processInstanceId != null
                && !processInstanceId.isBlank() && executionId != null && !executionId.isBlank());
        var process = processes.lockInstance(tenantId, processInstanceId);
        // Flowable also hosts processes that have no FlowMint ledger.
        if (process == null) {
            return;
        }
        require(tenantId.equals(process.getTenantId())
                && processInstanceId.equals(process.getProcessInstanceId()));
        cancelActive(tenantId, processInstanceId, executionId);
    }

    private void cancelActive(String tenantId, String processInstanceId, String executionId)
            throws ServiceException {
        // Current locking read is essential: the caller may have an older repeatable-read snapshot.
        var active = executions.lockActiveForProcess(tenantId, processInstanceId);
        require(active != null);
        for (var row : active) {
            require(row != null && tenantId.equals(row.getTenantId())
                    && processInstanceId.equals(row.getProcessInstanceId()) && "GROOVY".equals(row.getTaskType())
                    && ("READY".equals(row.getStatus()) || "RUNNING".equals(row.getStatus()))
                    && row.getGeneration() != null && row.getGeneration() >= 0
                    && row.getGeneration() < Integer.MAX_VALUE);
            if (executionId != null && !executionId.equals(row.getFlowableExecutionId())) {
                continue;
            }
            var now = Date.from(clock.instant());
            int cancelledAttempts = attempts.cancelRunning(tenantId, row.getOid(), row.getGeneration(), now);
            require(cancelledAttempts == ("RUNNING".equals(row.getStatus()) ? 1 : 0));
            require(executions.cancel(tenantId, row.getOid(), now) == 1);
        }
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_CANCEL_STATE_INVALID");
        }
    }
}
