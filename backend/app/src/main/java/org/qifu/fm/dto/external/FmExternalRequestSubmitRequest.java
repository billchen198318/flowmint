package org.qifu.fm.dto.external;

import java.util.Map;

public record FmExternalRequestSubmitRequest(
		String processDefId,
		Integer processVersionNo,
		String formId,
		Integer formVersionNo,
		String formSchemaHash,
		Map<String, Object> submission,
		String initiatorAccount,
		String applicantAccount,
		String applicantOrgUnitId,
		ExternalReference externalReference,
		String remark) {

	public record ExternalReference(String sourceSystem, String sourceDocumentType,
			String sourceDocumentNo) { }
}
