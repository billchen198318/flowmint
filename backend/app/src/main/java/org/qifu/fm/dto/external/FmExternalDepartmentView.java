package org.qifu.fm.dto.external;

import java.util.Date;

public record FmExternalDepartmentView(
		String orgUnitId,
		String orgUnitCode,
		String name,
		String shortName,
		String orgUnitType,
		String status,
		String parentOrgUnitId,
		String path,
		Integer levelDepth,
		Integer sortNo,
		Date effectiveFrom,
		Date effectiveTo) {
}
