package org.qifu.fm.domain.ai;

import com.fasterxml.jackson.databind.JsonNode;

public record FmAiAnalysisRequest(
		JsonNode context,
		String outputLanguage,
		Integer promptTemplateVersion) {
}
