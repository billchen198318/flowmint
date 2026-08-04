package org.qifu.fm.domain.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PreDestroy;

@Component
public class FmDataSourcePoolRegistry {

	private final IFmDataSourcePoolService poolService;
	private final FmDataSourcePasswordCipher passwordCipher;
	private final Map<String, HikariDataSource> dataSources = new HashMap<>();

	public FmDataSourcePoolRegistry(IFmDataSourcePoolService poolService,
			FmDataSourcePasswordCipher passwordCipher) {
		this.poolService = poolService;
		this.passwordCipher = passwordCipher;
	}

	public synchronized HikariDataSource get(String tenantId, String poolId)
			throws ServiceException {
		FmDataSourcePool pool = loadActivePool(tenantId, poolId);
		String cacheKey = tenantId + ":" + poolId + ":" + pool.getLockVersion();
		HikariDataSource existing = dataSources.get(cacheKey);
		if (existing != null && !existing.isClosed()) {
			return existing;
		}
		closeOlderVersions(tenantId, poolId, cacheKey);
		HikariDataSource created = new HikariDataSource(config(pool));
		dataSources.put(cacheKey, created);
		return created;
	}

	private FmDataSourcePool loadActivePool(String tenantId, String poolId)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("poolId", poolId);
		params.put("status", "ACTIVE");
		List<FmDataSourcePool> pools = Objects.requireNonNullElse(
				poolService.selectListByParams(params, "POOL_ID", "ASC").getValue(),
				List.of());
		return pools.stream().findFirst()
				.orElseThrow(() -> new ServiceException(
						BaseSystemMessage.parameterIncorrect()));
	}

	private HikariConfig config(FmDataSourcePool pool) throws ServiceException {
		HikariConfig config = new HikariConfig();
		config.setPoolName("fm-action-" + pool.getPoolCode());
		config.setDriverClassName(pool.getDriverClass());
		config.setJdbcUrl(pool.getJdbcUrl());
		config.setUsername(pool.getUsername());
		config.setPassword(passwordCipher.decrypt(pool.getPasswordContent()));
		config.setMaximumPoolSize(pool.getMaximumPoolSize());
		config.setMinimumIdle(pool.getMinimumIdle());
		config.setConnectionTimeout(pool.getConnectionTimeoutMs());
		config.setIdleTimeout(pool.getIdleTimeoutMs());
		config.setMaxLifetime(pool.getMaxLifetimeMs());
		if (StringUtils.isNotBlank(pool.getValidationQuery())) {
			config.setConnectionTestQuery(pool.getValidationQuery());
		}
		return config;
	}

	private void closeOlderVersions(String tenantId, String poolId,
			String retainedKey) {
		String prefix = tenantId + ":" + poolId + ":";
		dataSources.entrySet().removeIf(entry -> {
			boolean stale = entry.getKey().startsWith(prefix)
					&& !entry.getKey().equals(retainedKey);
			if (stale) {
				entry.getValue().close();
			}
			return stale;
		});
	}

	@PreDestroy
	public synchronized void close() {
		dataSources.values().forEach(HikariDataSource::close);
		dataSources.clear();
	}
}
