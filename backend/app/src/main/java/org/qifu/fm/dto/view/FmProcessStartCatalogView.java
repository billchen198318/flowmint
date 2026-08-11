package org.qifu.fm.dto.view;

public record FmProcessStartCatalogView(
        String processDefId,
        String processKey,
        String processName,
        String category,
        String description,
        Integer versionNo) {
}
