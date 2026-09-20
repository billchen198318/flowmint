package org.qifu.fm.domain.workflow;

import java.time.Instant;
import java.util.Set;

import org.qifu.fm.entity.FmSystemTaskExecution;

/** Only trusted pre-execution admission/launch failures qualify; unknown outcomes fail closed. */
public final class FmGroovyAutomaticRetryPolicy {

    public static final Set<String> RETRYABLE = Set.of("GROOVY_RUNTIME_BUSY", "GROOVY_RUNTIME_START_UNAVAILABLE");

    private FmGroovyAutomaticRetryPolicy() {
    }

    public static boolean due(FmSystemTaskExecution row, Instant now) {
        if (row == null || now == null || !"GROOVY".equals(row.getTaskType()) || !"FAILED".equals(row.getStatus())
                || row.getErrorCode() == null || !RETRYABLE.contains(row.getErrorCode())
                || row.getAttemptNo() == null || row.getAttemptNo() < 1 || row.getAttemptNo() > 2
                || row.getGeneration() == null || row.getGeneration() < 1 || row.getGeneration() >= Integer.MAX_VALUE - 1
                || row.getLeaseUntil() != null || row.getCompletedAt() == null
                || row.getResultSha256() != null || row.getFormSnapshotOid() != null) {
            return false;
        }
        long seconds = row.getAttemptNo() == 1 ? 1 : 5;
        return !row.getCompletedAt().toInstant().plusSeconds(seconds).isAfter(now);
    }
}
