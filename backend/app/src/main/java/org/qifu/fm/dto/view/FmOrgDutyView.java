package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmOrgDutyView(
		String oid,
		String tenantId,
		String dutyId,
		String orgUnitId,
		String orgUnitLabel,
		String dutyCode,
		String dutyName,
		String dutyType,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description,
		List<FmEmployeeDutyView> assignees) {
}
