package org.qifu.fm.dto.command;

public record FmTaskReassignPreviewRequest(
        String taskId,
        String targetAccount) {
}
