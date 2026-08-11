package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmApprovalAuthorityView(
        String oid,
        String tenantId,
        String approvalAuthorityId,
        String authorityCode,
        String authorityName,
        String processDefId,
        String formId,
        String status,
        Date effectiveFrom,
        Date effectiveTo,
        String description,
        List<FmApprovalAuthorityRuleView> rules) {
}
