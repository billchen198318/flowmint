package org.qifu.fm.dto.command;

public record FmApprovalGroupCommand(
		String oid,
		String tenantId,
		String groupCode,
		String groupName,
		String assignmentMode,
		String status,
		String description) {
}
