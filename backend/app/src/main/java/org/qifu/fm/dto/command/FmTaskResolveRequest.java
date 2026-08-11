package org.qifu.fm.dto.command;

public record FmTaskResolveRequest(
        String taskId,
        String comment) {
}
