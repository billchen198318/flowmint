package org.qifu.fm.dto.command;

public record FmDataSourcePoolCommand(
		String oid,
		String tenantId,
		String poolCode,
		String poolName,
		String dbType,
		String jdbcUrl,
		String username,
		String password,
		Integer maximumPoolSize,
		Integer minimumIdle,
		Long connectionTimeoutMs,
		Long idleTimeoutMs,
		Long maxLifetimeMs,
		String validationQuery,
		String status,
		Integer lockVersion,
		String description) {
}
