package org.qifu.fm.dto.command;

public record FmFormDefCommand(
        String oid,
        String tenantId,
        String formCode,
        String formName,
        String description) {
}
