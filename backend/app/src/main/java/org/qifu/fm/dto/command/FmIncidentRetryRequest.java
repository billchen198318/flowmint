package org.qifu.fm.dto.command;

public record FmIncidentRetryRequest(String incidentId, String reason) {
}
