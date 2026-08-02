package org.qifu.fm.dto.command;

import java.util.Date;

public record FmApprovalGroupMemberCommand(
		String oid,
		String groupOid,
		String employeeId,
		Integer priority,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
