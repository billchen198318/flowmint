package org.qifu.fm.dto.view;

import java.util.Date;

public record FmEmployeeDutyView(
		String oid,
		String employeeDutyId,
		String employeeOrgAssignmentId,
		String employeeLabel,
		String isPrimary,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
