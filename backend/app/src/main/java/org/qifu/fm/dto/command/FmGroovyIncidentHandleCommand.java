package org.qifu.fm.dto.command;

public record FmGroovyIncidentHandleCommand(String invocationId, String requestId,
        int expectedRevision, String targetStatus, String reason) {
}
