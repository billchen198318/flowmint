package org.qifu.fm.dto.command;

public record FmTaskTransferRequest(
        String taskId,
        String targetAccount,
        String comment,
        String reason) {
}
