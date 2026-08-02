package org.qifu.fm.dto.view;

import java.util.Date;

public record FmOrgUnitHeadView(
		String oid,
		String tenantId,
		String orgUnitHeadId,
		String orgUnitId,
		String orgUnitLabel,
		String employeeId,
		String employeeLabel,
		String headType,
		Integer priority,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description) {
}
