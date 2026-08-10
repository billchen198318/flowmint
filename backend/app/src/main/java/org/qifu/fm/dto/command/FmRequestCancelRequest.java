package org.qifu.fm.dto.command;

public record FmRequestCancelRequest(
        String processInstanceId,
        String reason) {
}
