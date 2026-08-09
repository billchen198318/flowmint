package org.qifu.fm.dto.command;

public record FmResolverPreviewCommand(
        String versionOid,
        String initiatorAccount) {
}
