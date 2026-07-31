package org.qifu.fm.dto.command;

import java.util.Date;

public record FmOrgUnitCommand(
		String oid,
		String tenantId,
		String unitCode,
		Integer currentVersionNo,
		String parentOrgUnitId,
		String unitName,
		String shortName,
		String unitType,
		Integer sortNo,
		String isVirtual,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description) {
}
