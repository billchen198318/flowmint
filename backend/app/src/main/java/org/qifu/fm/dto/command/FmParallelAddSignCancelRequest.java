package org.qifu.fm.dto.command;

public record FmParallelAddSignCancelRequest(
        String taskId,
        String reason) {
}
