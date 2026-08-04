package org.qifu.fm.dto.view;

import java.util.Map;

public record FmDataActionExecutionView(
		String executionId,
		String actionCode,
		Integer versionNo,
		boolean rolledBack,
		Map<String, Object> data) {
}
