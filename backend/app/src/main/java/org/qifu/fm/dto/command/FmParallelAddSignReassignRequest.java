package org.qifu.fm.dto.command;

public record FmParallelAddSignReassignRequest(
        String taskId,
        String targetAccount,
        String reason) {
}
