package org.qifu.fm.dto.command;

public record FmTaskActionRequest(
        String taskId,
        String actionType,
        String comment,
        String reason,
        String targetTaskDefKey) {
}
