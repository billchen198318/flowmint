package org.qifu.fm.dto.view;

import java.util.List;

public record FmDataActionView(
		String oid,
		String tenantId,
		String actionId,
		String actionCode,
		String actionName,
		String poolId,
		String actionType,
		String requestSchema,
		String responseMode,
		String status,
		Integer currentVersionNo,
		Integer draftVersionNo,
		String draftStatus,
		Integer lockVersion,
		Integer rateLimitPerMinute,
		String description,
		List<FmDataActionStepView> steps) {
}
