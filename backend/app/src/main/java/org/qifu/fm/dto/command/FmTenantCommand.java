package org.qifu.fm.dto.command;

public record FmTenantCommand(
		String oid,
		String tenantId,
		String tenantCode,
		String tenantName,
		String defaultLocale,
		String defaultTimezone,
		String status,
		String description) {
}