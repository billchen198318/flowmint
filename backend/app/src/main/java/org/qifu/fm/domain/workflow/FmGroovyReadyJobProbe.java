package org.qifu.fm.domain.workflow;

import java.util.List;

import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.job.api.Job;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.springframework.stereotype.Component;

/** Read-only engine inspection. Recovery never invents or executes a replacement job. */
@Component
public class FmGroovyReadyJobProbe {
    public enum State { WAITING, DEAD_LETTER, MISSING, EXECUTION_REMOVED }

    private final RuntimeService runtime;
    private final ManagementService management;

    public FmGroovyReadyJobProbe(RuntimeService runtime, ManagementService management) {
        this.runtime = runtime;
        this.management = management;
    }

    public State inspect(FmSystemTaskExecution row, String definitionId) throws ServiceException {
        require(row != null && text(row.getTenantId()) && text(row.getFlowableExecutionId())
                && text(row.getProcessInstanceId()) && text(row.getNodeId()) && text(definitionId));
        var execution = runtime.createExecutionQuery().executionId(row.getFlowableExecutionId()).singleResult();
        if (execution == null) return State.EXECUTION_REMOVED;
        require(row.getTenantId().equals(execution.getTenantId())
                && row.getFlowableExecutionId().equals(execution.getId())
                && row.getProcessInstanceId().equals(execution.getProcessInstanceId()));
        if (execution.isEnded() || !row.getNodeId().equals(execution.getActivityId())) return State.EXECUTION_REMOVED;
        var instance = runtime.createProcessInstanceQuery().processInstanceId(row.getProcessInstanceId()).singleResult();
        if (instance == null) return State.EXECUTION_REMOVED;
        require(row.getTenantId().equals(instance.getTenantId()) && row.getProcessInstanceId().equals(instance.getId())
                && definitionId.equals(instance.getProcessDefinitionId()));
        if (instance.isEnded()) return State.EXECUTION_REMOVED;
        if (execution.isSuspended() || instance.isSuspended()) return State.WAITING;

        var active = management.createJobQuery().executionId(row.getFlowableExecutionId()).listPage(0, 2);
        var timers = management.createTimerJobQuery().executionId(row.getFlowableExecutionId()).listPage(0, 2);
        var suspended = management.createSuspendedJobQuery().executionId(row.getFlowableExecutionId()).listPage(0, 2);
        var dead = management.createDeadLetterJobQuery().executionId(row.getFlowableExecutionId()).listPage(0, 2);
        require(active != null && timers != null && suspended != null && dead != null);
        int count = active.size() + timers.size() + suspended.size() + dead.size();
        require(count <= 1);
        if (count == 0) return State.MISSING;
        var identity = new FmGroovyJobIdentity(row.getTenantId(), row.getFlowableExecutionId(),
                row.getProcessInstanceId(), definitionId, row.getNodeId());
        for (List<? extends Job> group : List.of(active, timers, suspended, dead)) {
            // The same execution may revisit this node in a loop. Never mutate its newer occurrence.
            if (!group.isEmpty() && !row.getInvocationId().equals(identity.invocation(group.getFirst()))) {
                return State.EXECUTION_REMOVED;
            }
        }
        return dead.isEmpty() ? State.WAITING : State.DEAD_LETTER;
    }

    private boolean text(String value) {
        return value != null && !value.isBlank();
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) throw new ServiceException("GROOVY_READY_JOB_IDENTITY_INVALID");
    }
}
