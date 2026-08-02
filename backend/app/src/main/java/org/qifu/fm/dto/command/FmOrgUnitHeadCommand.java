package org.qifu.fm.dto.command;

import java.util.Date;

public record FmOrgUnitHeadCommand(
		String oid,
		String tenantId,
		String orgUnitId,
		String employeeId,
		String headType,
		Integer priority,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description) {
}
