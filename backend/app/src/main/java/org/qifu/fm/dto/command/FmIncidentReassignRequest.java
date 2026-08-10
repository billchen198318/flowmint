package org.qifu.fm.dto.command;

public record FmIncidentReassignRequest(
        String incidentId,
        String targetAccount,
        String reason) {
}
