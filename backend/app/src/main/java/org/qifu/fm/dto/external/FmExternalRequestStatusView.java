package org.qifu.fm.dto.external;

import java.util.Date;
import java.util.List;

public record FmExternalRequestStatusView(
		String processInstanceId,
		String businessKey,
		String documentNumber,
		FmExternalRequestSubmitRequest.ExternalReference externalReference,
		String processDefId,
		String processName,
		Integer processVersionNo,
		String formId,
		Integer formVersionNo,
		String initiatorAccount,
		String applicantAccount,
		String applicantOrgUnitId,
		String status,
		String runtimeStatus,
		String statusLabel,
		Date submittedAt,
		Date lastChangedAt,
		Date completedAt,
		List<ActiveTask> activeTasks,
		Incident incident,
		List<TimelineEntry> timeline,
		boolean timelineTruncated) {

	public record ActiveTask(String taskDefinitionKey, String taskName,
			String assignmentState, Date startedAt, Date dueAt) { }

	public record Incident(String incidentId, String errorCode,
			String taskDefinitionKey, Date openedAt, String operationsStatus) { }

	public record TimelineEntry(String actionType, String taskName,
			Date occurredAt, String result) { }
}
