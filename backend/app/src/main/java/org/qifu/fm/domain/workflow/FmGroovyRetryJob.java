package org.qifu.fm.domain.workflow;

import org.flowable.engine.ManagementService;
import org.flowable.job.api.Job;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.springframework.stereotype.Component;

/** Restores exactly one dead-letter occurrence; never executes worker IO in the operator transaction. */
@Component
public class FmGroovyRetryJob {

    private final ManagementService management;
    private final FmGroovyFlowableTransactionGuard transactions;

    public FmGroovyRetryJob(ManagementService management, FmGroovyFlowableTransactionGuard transactions) {
        this.management = management;
        this.transactions = transactions;
    }

    public void check(FmSystemTaskExecution row, String definitionId) throws ServiceException {
        deadLetter(row, definitionId);
    }

    public void restore(FmSystemTaskExecution row, String definitionId) throws ServiceException {
        try {
            management.executeCommand(context -> {
                try {
                    transactions.require();
                    Job dead = deadLetter(row, definitionId);
                    Job restored = management.moveDeadLetterJobToExecutableJob(dead.getId(), 1);
                    require(identity(row, definitionId).invocation(restored).equals(row.getInvocationId())
                            && restored.getRetries() == 1);
                    return null;
                } catch (ServiceException invalid) {
                    throw new IllegalStateException("GROOVY_RETRY_JOB_INVALID");
                }
            });
        } catch (RuntimeException failure) {
            throw new ServiceException("GROOVY_RETRY_JOB_INVALID");
        }
    }

    /** A recalculation has a new occurrence correlation; an ordinary retry must never call this method. */
    public String recalculate(FmSystemTaskExecution row, String definitionId, String correlationId)
            throws ServiceException {
        String invocationId = FmGroovyJobIdentity.invocationForCorrelation(row.getTenantId(), correlationId);
        require(!invocationId.equals(row.getInvocationId()));
        try {
            return management.executeCommand(context -> {
                try {
                    transactions.require();
                    Job dead = deadLetter(row, definitionId);
                    require(dead instanceof DeadLetterJobEntity);
                    // The queried entity belongs to this command's cache. Move copies its correlation
                    // into the newly inserted job before scheduling the executor's after-commit callback.
                    ((DeadLetterJobEntity) dead).setCorrelationId(correlationId);
                    Job restored = management.moveDeadLetterJobToExecutableJob(dead.getId(), 1);
                    require(invocationId.equals(identity(row, definitionId).invocation(restored))
                            && restored.getRetries() == 1 && !restored.getId().equals(row.getFirstJobId()));
                    return restored.getId();
                } catch (ServiceException invalid) {
                    throw new IllegalStateException("GROOVY_RECALCULATE_JOB_INVALID");
                }
            });
        } catch (RuntimeException failure) {
            throw new ServiceException("GROOVY_RECALCULATE_JOB_INVALID");
        }
    }

    private Job deadLetter(FmSystemTaskExecution row, String definitionId) throws ServiceException {
        require(management.createJobQuery().executionId(row.getFlowableExecutionId()).count() == 0
                && management.createTimerJobQuery().executionId(row.getFlowableExecutionId()).count() == 0
                && management.createSuspendedJobQuery().executionId(row.getFlowableExecutionId()).count() == 0);
        var jobs = management.createDeadLetterJobQuery().executionId(row.getFlowableExecutionId()).listPage(0, 2);
        require(jobs != null && jobs.size() == 1);
        Job job = jobs.getFirst();
        require(identity(row, definitionId).invocation(job).equals(row.getInvocationId()));
        return job;
    }

    private FmGroovyJobIdentity identity(FmSystemTaskExecution row, String definitionId) {
        return new FmGroovyJobIdentity(row.getTenantId(), row.getFlowableExecutionId(),
                row.getProcessInstanceId(), definitionId, row.getNodeId());
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) throw new ServiceException("GROOVY_RETRY_JOB_INVALID");
    }
}
