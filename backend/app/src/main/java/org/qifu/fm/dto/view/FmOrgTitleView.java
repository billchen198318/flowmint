package org.qifu.fm.dto.view;

import java.util.Date;
import org.qifu.fm.entity.FmOrgTitle;

public record FmOrgTitleView(String oid, String tenantId, String titleId, String orgUnitId,
    String titleCode, String titleName, String approvalLevelId, String isManagerTitle,
    Integer sortNo, String status, Date effectiveFrom, Date effectiveTo, String description) {
  public static FmOrgTitleView from(FmOrgTitle value) {
    return new FmOrgTitleView(value.getOid(), value.getTenantId(), value.getTitleId(),
        value.getOrgUnitId(), value.getTitleCode(), value.getTitleName(), value.getApprovalLevelId(),
        value.getIsManagerTitle(), value.getSortNo(), value.getStatus(), value.getEffectiveFrom(),
        value.getEffectiveTo(), value.getDescription());
  }
}
