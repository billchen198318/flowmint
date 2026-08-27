package org.qifu.fm.dto.command;

import java.util.List;

public record FmDataActionCommand(
		String oid,
		String tenantId,
		String actionCode,
		String actionName,
		String poolId,
		String actionType,
		String requestSchema,
		String responseMode,
		String status,
		Integer versionNo,
		Integer lockVersion,
		Integer rateLimitPerMinute,
		String description,
		List<FmDataActionStepCommand> steps) {
}
