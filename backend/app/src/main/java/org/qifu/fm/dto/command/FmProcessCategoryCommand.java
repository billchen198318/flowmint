package org.qifu.fm.dto.command;

public record FmProcessCategoryCommand(
        String oid,
        String tenantId,
        String categoryCode,
        String categoryLabel,
        String iconCode,
        Integer sortOrder,
        String status) {
}
