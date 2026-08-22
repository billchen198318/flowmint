package org.qifu.fm.dto.view;

public record FmProcessCategoryView(
        String oid,
        String tenantId,
        String categoryCode,
        String categoryLabel,
        String iconCode,
        Integer sortOrder,
        String status) {
}
