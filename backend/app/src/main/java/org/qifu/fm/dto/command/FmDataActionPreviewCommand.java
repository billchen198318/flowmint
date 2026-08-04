package org.qifu.fm.dto.command;

import java.util.Map;

public record FmDataActionPreviewCommand(
		String tenantId,
		Map<String, Object> request,
		Boolean commit) {
}
