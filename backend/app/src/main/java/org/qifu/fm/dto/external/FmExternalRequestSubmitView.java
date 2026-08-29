package org.qifu.fm.dto.external;

import java.util.Date;

public record FmExternalRequestSubmitView(
		String processInstanceId,
		String flowableProcessInstanceId,
		String businessKey,
		String documentNumber,
		String formDataId,
		String processDefId,
		Integer processVersionNo,
		String formId,
		Integer formVersionNo,
		String status,
		Date submittedAt,
		boolean idempotentReplay) {
}
