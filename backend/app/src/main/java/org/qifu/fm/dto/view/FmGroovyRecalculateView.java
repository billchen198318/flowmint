package org.qifu.fm.dto.view;

/** Preview exposes revisions and hashes only, not live business data or script contents. */
public record FmGroovyRecalculateView(String invocationId, int revision, int generation,
        int previousFormRevision, int formRevision, int formLock, String inputSha256, String bindingSha256) {
}
