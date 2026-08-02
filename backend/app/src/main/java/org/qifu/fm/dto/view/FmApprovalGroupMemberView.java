package org.qifu.fm.dto.view;

import java.util.Date;

public record FmApprovalGroupMemberView(
		String oid,
		String approvalGroupMemberId,
		String employeeId,
		String employeeLabel,
		Integer priority,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}
