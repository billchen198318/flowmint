package org.qifu.fm.dto.command;

public record FmProcessTerminateRequest(String processInstanceId, String reason) {
}
