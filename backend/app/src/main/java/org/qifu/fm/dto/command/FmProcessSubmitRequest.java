package org.qifu.fm.dto.command;

import java.util.Map;

public record FmProcessSubmitRequest(
		String processDefId,
		String formId,
		Integer formVersionNo,
		String applicantAccount,
		Map<String, Object> formData) {
}
