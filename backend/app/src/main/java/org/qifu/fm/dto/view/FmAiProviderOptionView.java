package org.qifu.fm.dto.view;

public record FmAiProviderOptionView(
		String providerCode,
		String displayName,
		String providerType,
		String modelId,
		boolean defaultProvider) {
}
