package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessDefView(
        String oid,
        String tenantId,
        String processDefId,
        String processKey,
        String processName,
        String category,
        String documentType,
        Integer currentVersionNo,
        String status,
        String description,
        List<FmProcessVersionView> versions) {
}
