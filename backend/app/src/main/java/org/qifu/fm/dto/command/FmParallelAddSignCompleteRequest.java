package org.qifu.fm.dto.command;

public record FmParallelAddSignCompleteRequest(
        String taskId,
        String result,
        String comment) {
}
