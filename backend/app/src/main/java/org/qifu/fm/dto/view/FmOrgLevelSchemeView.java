package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;

public record FmOrgLevelSchemeView(
  String oid,
  String tenantId,
  String levelSchemeId,
  String schemeCode,
  String schemeName,
  String isDefault,
  String status,
  Date effectiveFrom,
  Date effectiveTo,
  String description,
  List<FmOrgApprovalLevel> levels
) {
  public static FmOrgLevelSchemeView from(
    FmOrgLevelScheme v,
    List<FmOrgApprovalLevel> x
  ) {
    return new FmOrgLevelSchemeView(
      v.getOid(),
      v.getTenantId(),
      v.getLevelSchemeId(),
      v.getSchemeCode(),
      v.getSchemeName(),
      v.getIsDefault(),
      v.getStatus(),
      v.getEffectiveFrom(),
      v.getEffectiveTo(),
      v.getDescription(),
      x
    );
  }
}
