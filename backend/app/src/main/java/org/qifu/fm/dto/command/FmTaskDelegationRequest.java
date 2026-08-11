package org.qifu.fm.dto.command;

public record FmTaskDelegationRequest(
        String taskId,
        String delegationId,
        String comment,
        String reason) {
}
