package org.qifu.fm.dto.view;

import java.math.BigDecimal;
import java.util.Date;

public record FmAiProviderView(
		String oid,
		String tenantId,
		String providerCode,
		String providerType,
		String displayName,
		String baseUrl,
		String modelId,
		boolean apiKeyConfigured,
		String maskedApiKey,
		BigDecimal temperature,
		Integer maxOutputTokens,
		Integer timeoutSeconds,
		String defaultFlag,
		Integer configVersion,
		String status,
		String lastTestStatus,
		Date lastTestDate,
		Integer lockVersion) {
}
