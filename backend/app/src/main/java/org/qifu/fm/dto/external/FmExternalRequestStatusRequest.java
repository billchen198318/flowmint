package org.qifu.fm.dto.external;

public record FmExternalRequestStatusRequest(
		String processInstanceId,
		String businessKey,
		String documentNumber,
		FmExternalRequestSubmitRequest.ExternalReference externalReference,
		Boolean includeTimeline) {
}
