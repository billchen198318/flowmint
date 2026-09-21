package org.qifu.fm.dto.view;

import java.util.Date;

/** Safe operations projection; deliberately excludes script, input, context and worker diagnostics. */
public record FmGroovyIncidentView(String incidentId, String invocationId, String processInstanceId,
        String processDefId, Integer versionNo, String nodeId, Integer attemptNo,
        String failureCategory, Date startedAt, Date completedAt, String executionStatus, String parentInvocationId) {
}
