package org.qifu.fm.dto.view;

import java.util.Date;

public record FmEmployeeOrgAssignmentView(
		String oid,
		String employeeOrgAssignmentId,
		String orgUnitId,
		String orgUnitLabel,
		String titleId,
		String titleLabel,
		String managerSource,
		String directManagerAssignmentId,
		String directManagerLabel,
		String isPrimary,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
