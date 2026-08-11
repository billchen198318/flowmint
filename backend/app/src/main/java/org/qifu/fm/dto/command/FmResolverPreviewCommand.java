package org.qifu.fm.dto.command;

import java.util.Map;

public record FmResolverPreviewCommand(
        String versionOid,
        String initiatorAccount,
        Map<String, Object> variables) {
}
