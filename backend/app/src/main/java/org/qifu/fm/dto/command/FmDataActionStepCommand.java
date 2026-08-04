package org.qifu.fm.dto.command;

public record FmDataActionStepCommand(
		String oid,
		String stepCode,
		String stepName,
		Integer executionOrder,
		String statementType,
		String executionMode,
		String sqlContent,
		String arrayPath,
		String resultKey,
		String resultMode,
		Integer expectAffectedRows,
		String continueCondition,
		Integer queryTimeoutSeconds,
		Integer maxRows,
		String status) {
}
