package org.qifu.fm.dto.command;

public record FmProcessStartLoadCommand(
		String tenantId,
		String processDefId,
		String applicantAccount) {
}
