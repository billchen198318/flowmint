package org.qifu.fm.dto.command;

import java.util.Date;

public record FmEmployeeCommand(
		String oid,
		String tenantId,
		String employeeNo,
		String account,
		String displayName,
		String email,
		String mobile,
		String locale,
		String timezone,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String description) {
}
