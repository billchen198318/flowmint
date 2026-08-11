package org.qifu.fm.dto.view;

import java.util.Date;

public record FmAssignmentIncidentView(
        String incidentId,
        String processInstanceId,
        String taskId,
        String taskDefKey,
        String incidentType,
        String errorCode,
        String errorMessage,
        String contextData,
        String incidentStatus,
        String resolvedBy,
        Date resolvedDate,
        String resolutionNote,
        Date createdDate) {
}
