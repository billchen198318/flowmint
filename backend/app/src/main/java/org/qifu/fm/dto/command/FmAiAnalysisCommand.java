package org.qifu.fm.dto.command;

public record FmAiAnalysisCommand(
		String taskId,
		String providerCode,
		Boolean forceRefresh) {
}
