package org.qifu.fm.dto.view;

import java.util.Date;

public record FmDataActionExecutionAuditView(
		String executionId,
		String tenantId,
		String actionCode,
		Integer versionNo,
		String loginAccount,
		String executionStatus,
		String rollbackOnly,
		Integer stepCount,
		Integer requestParameterCount,
		Date startTime,
		Date endTime,
		Long durationMs,
		String errorMessage) {
}
