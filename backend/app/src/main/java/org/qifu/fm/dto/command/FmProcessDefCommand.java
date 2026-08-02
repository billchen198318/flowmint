package org.qifu.fm.dto.command;

public record FmProcessDefCommand(
        String oid,
        String tenantId,
        String processKey,
        String processName,
        String category,
        String description) {
}