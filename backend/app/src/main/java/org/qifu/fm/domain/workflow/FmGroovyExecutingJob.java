package org.qifu.fm.domain.workflow;

import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.job.api.Job;
import org.flowable.job.service.impl.cmd.ExecuteAsyncRunnableJobCmd;
import org.flowable.job.service.impl.cmd.ExecuteJobCmd;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

/** Flowable 8 command adapter; the current job is pending deletion while the agenda runs. */
@Component
public class FmGroovyExecutingJob {

    public Job current() throws ServiceException {
        var context = Context.getCommandContext();
        if (context == null) {
            throw new ServiceException("GROOVY_JOB_IDENTITY_INVALID");
        }
        String jobId;
        if (context.getCommand() instanceof ExecuteAsyncRunnableJobCmd command) {
            jobId = command.getJobId();
        } else if (context.getCommand() instanceof ExecuteJobCmd command) {
            jobId = command.getJobId();
        } else {
            throw new ServiceException("GROOVY_JOB_IDENTITY_INVALID");
        }
        // Do not query executable jobs: that query may exclude this command's pending deletion.
        // The deletion and engine advance still share the original transaction and revision check.
        var cache = context.getSession(EntityCache.class);
        JobEntity job = jobId == null || cache == null ? null : cache.findInCache(JobEntity.class, jobId);
        if (job == null || !jobId.equals(job.getId())) {
            throw new ServiceException("GROOVY_JOB_IDENTITY_INVALID");
        }
        return job;
    }
}
