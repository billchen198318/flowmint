package org.qifu.fm.dto.view;

public record FmDataSourcePoolView(
		String oid,
		String tenantId,
		String poolId,
		String poolCode,
		String poolName,
		String dbType,
		String driverClass,
		String jdbcUrl,
		String username,
		boolean passwordConfigured,
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
