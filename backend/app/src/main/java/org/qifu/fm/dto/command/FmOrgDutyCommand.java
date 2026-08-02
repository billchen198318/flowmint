package org.qifu.fm.dto.command;

import java.util.Date;

public record FmOrgDutyCommand(
		String oid,
		String tenantId,
		String orgUnitId,
		String dutyCode,
		String dutyName,
		String dutyType,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description) {
}
