package org.qifu.fm.domain.ai;

import tools.jackson.databind.JsonNode;

public record FmAiAnalysisResponse(
		JsonNode result,
		Integer inputTokens,
		Integer outputTokens,
		String providerResponseId) {
}
