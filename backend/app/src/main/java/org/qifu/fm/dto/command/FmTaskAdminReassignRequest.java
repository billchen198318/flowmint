package org.qifu.fm.dto.command;

public record FmTaskAdminReassignRequest(
        String taskId,
        String targetAccount,
        String reason,
        String requestKey) {
}
