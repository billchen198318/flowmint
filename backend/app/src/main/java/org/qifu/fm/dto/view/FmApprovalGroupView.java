package org.qifu.fm.dto.view;

import java.util.List;

public record FmApprovalGroupView(
		String oid,
		String tenantId,
		String approvalGroupId,
		String groupCode,
		String groupName,
		String assignmentMode,
		String status,
		String description,
		List<FmApprovalGroupMemberView> members) {
}
