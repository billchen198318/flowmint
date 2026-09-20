package org.qifu.fm.dto.command;

public record FmGroovyRecalculateCommand(String invocationId, String requestId, int expectedRevision,
        int expectedGeneration, int formRevision, int formLock, String inputSha256, String bindingSha256,
        String reason) {
}
