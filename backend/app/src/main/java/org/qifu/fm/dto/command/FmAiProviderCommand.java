package org.qifu.fm.dto.command;

import java.math.BigDecimal;

public record FmAiProviderCommand(
		String oid,
		String tenantId,
		String providerCode,
		String providerType,
		String displayName,
		String baseUrl,
		String modelId,
		String apiKey,
		BigDecimal temperature,
		Integer maxOutputTokens,
		Integer timeoutSeconds,
		String defaultFlag,
		String status,
		Integer lockVersion) {
}
