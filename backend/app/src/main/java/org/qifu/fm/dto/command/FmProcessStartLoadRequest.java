package org.qifu.fm.dto.command;

public record FmProcessStartLoadRequest(
		String processDefId,
		String applicantAccount) {
}
