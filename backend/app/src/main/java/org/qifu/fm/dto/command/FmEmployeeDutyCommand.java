package org.qifu.fm.dto.command;

import java.util.Date;

public record FmEmployeeDutyCommand(
		String oid,
		String dutyOid,
		String employeeOrgAssignmentId,
		String isPrimary,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
