package org.qifu.fm.dto.view;

public record FmProcessStartCatalogView(
        String processDefId,
        String processKey,
        String processName,
        String categoryCode,
        String categoryLabel,
        String categoryIcon,
        Integer categorySortOrder,
        Integer processSortOrder,
        String description,
        Integer versionNo) {
}
