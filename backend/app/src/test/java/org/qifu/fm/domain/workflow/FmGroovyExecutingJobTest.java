package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.common.engine.impl.persistence.cache.EntityCacheImpl;
import org.flowable.job.service.impl.cmd.ExecuteAsyncRunnableJobCmd;
import org.flowable.job.service.impl.cmd.ExecuteJobCmd;
import org.flowable.job.service.impl.persistence.entity.JobEntityImpl;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmGroovyExecutingJobTest {

    private final FmGroovyExecutingJob adapter = new FmGroovyExecutingJob();

    @Test
    void resolvesPendingDeletionByActualAsyncCommandEvenWithOtherCachedJobs() throws Exception {
        verifyPendingDeletion(new ExecuteAsyncRunnableJobCmd("current", null, null, false));
    }

    @Test
    void explicitExecuteJobUsesSameCommandIdentity() throws Exception {
        verifyPendingDeletion(new ExecuteJobCmd("current", null));
    }

    @Test
    void missingContextUnsupportedCommandAndAbsentJobAreRejected() {
        assertThrows(ServiceException.class, adapter::current);
        for (Command<?> command : new Command<?>[] { context -> null, new ExecuteJobCmd("absent", null) }) {
            var context = new CommandContext(command);
            context.getSessions().put(EntityCache.class, new EntityCacheImpl());
            Context.setCommandContext(context);
            try {
                assertThrows(ServiceException.class, adapter::current);
            } finally {
                Context.removeCommandContext();
            }
        }
    }

    private void verifyPendingDeletion(Command<?> command) throws Exception {
        var context = new CommandContext(command);
        var cache = new EntityCacheImpl();
        var job = new JobEntityImpl();
        job.setId("current");
        cache.put(job, false);
        job.setDeleted(true);
        var other = new JobEntityImpl();
        other.setId("other");
        cache.put(other, false);
        context.getSessions().put(EntityCache.class, cache);
        Context.setCommandContext(context);
        try {
            assertSame(job, adapter.current());
        } finally {
            Context.removeCommandContext();
        }
    }
}
