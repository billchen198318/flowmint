package org.qifu.fm.domain.ai;

import java.math.BigDecimal;

public record FmAiProviderConfig(
		String providerCode,
		String providerType,
		String baseUrl,
		String modelId,
		String apiKey,
		BigDecimal temperature,
		Integer maxOutputTokens,
		Integer timeoutSeconds) {
}
