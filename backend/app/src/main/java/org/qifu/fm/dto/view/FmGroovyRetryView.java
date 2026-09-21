package org.qifu.fm.dto.view;

/** No input content, context, script or raw diagnostics leave the operations service. */
public record FmGroovyRetryView(String invocationId, int revision, int generation,
        Integer inputRevision, String inputSha256, String bindingSha256, int remainingAttempts) {
}
