package org.qifu.fm.dto.command;

import java.util.Date;

public record FmEmployeeOrgAssignmentCommand(
		String oid,
		String employeeOid,
		String orgUnitId,
		String titleId,
		String managerSource,
		String directManagerAssignmentId,
		String isPrimary,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
