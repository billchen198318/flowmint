package org.qifu.fm.dto.view;

import java.util.List;

import tools.jackson.databind.JsonNode;

public record FmGroovyPreviewView(String invocationId, String bindingSha256, String manifestSha256,
        String status, String errorCode, JsonNode result, List<String> logs, boolean logsTruncated,
        List<Diagnostic> diagnostics, long elapsedMs, Integer expectedLockVersion) {

    public record Diagnostic(String code, int line, int column) {
    }
}
