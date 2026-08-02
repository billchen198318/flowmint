package org.qifu.fm.dto.command;

import java.util.Date;

public record FmOrgTitleCommand(String oid, String tenantId, String titleCode,
    String titleName, String approvalLevelId, String isManagerTitle, Integer sortNo, String status,
    Date effectiveFrom, Date effectiveTo, String description) {}
