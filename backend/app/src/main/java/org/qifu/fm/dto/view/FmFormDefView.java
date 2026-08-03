package org.qifu.fm.dto.view;

import java.util.List;

public record FmFormDefView(
        String oid,
        String tenantId,
        String formId,
        String formCode,
        String formName,
        Integer currentVersionNo,
        String status,
        String description,
        List<FmFormVersionView> versions) {
}
