package org.qifu.fm.dto.command;

public record FmRequestWithdrawRequest(
        String processInstanceId,
        String reason) {
}
