package org.qifu.fm.dto.view;

import tools.jackson.databind.JsonNode;

public record FmAiAnalysisView(
		String analysisId,
		String providerCode,
		String providerType,
		String modelId,
		Integer generationNo,
		boolean cacheHit,
		JsonNode result,
		Integer inputTokens,
		Integer outputTokens,
		String disclaimer) {
}
