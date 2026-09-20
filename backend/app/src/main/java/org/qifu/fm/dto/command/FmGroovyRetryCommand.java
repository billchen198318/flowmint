package org.qifu.fm.dto.command;

public record FmGroovyRetryCommand(String invocationId, String requestId, int expectedRevision,
        int expectedGeneration, String reason) {
}
