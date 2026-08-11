package org.qifu.fm.dto.command;

public record FmTaskAddSignRequest(
        String taskId,
        String targetAccount,
        String comment,
        String reason) {
}
