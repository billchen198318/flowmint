package org.qifu.fm.dto.command;

import java.util.Date;
import java.util.List;

public record FmApprovalAuthorityCommand(
        String oid,
        String tenantId,
        String authorityCode,
        String authorityName,
        String processDefId,
        String formId,
        String status,
        Date effectiveFrom,
        Date effectiveTo,
        String description,
        List<FmApprovalAuthorityRuleCommand> rules) {
}
