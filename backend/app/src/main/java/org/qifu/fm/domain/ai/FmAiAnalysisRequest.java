package org.qifu.fm.domain.ai;

import tools.jackson.databind.JsonNode;

public record FmAiAnalysisRequest(
		JsonNode context,
		String outputLanguage,
		Integer promptTemplateVersion) {
}
