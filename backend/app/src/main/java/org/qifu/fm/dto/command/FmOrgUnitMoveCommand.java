package org.qifu.fm.dto.command;

public record FmOrgUnitMoveCommand(
		String tenantId,
		String orgUnitId,
		String newParentOrgUnitId,
		Integer currentVersionNo,
		Integer sortNo) {
}
