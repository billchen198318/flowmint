package org.qifu.fm.logic.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.domain.datasource.FmDataSourceDriverCatalog;
import org.qifu.fm.domain.datasource.FmDataSourcePasswordCipher;
import org.qifu.fm.dto.command.FmDataSourcePoolCommand;
import org.qifu.fm.dto.view.FmDataSourcePoolView;
import org.qifu.fm.dto.view.FmDataSourceTestView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.logic.IFmDataSourcePoolLogicService;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Service
@Transactional(readOnly = true)
public class FmDataSourcePoolLogicServiceImpl implements IFmDataSourcePoolLogicService {

	private final IFmDataSourcePoolService poolService;
	private final IFmTenantService tenantService;
	private final FmDataSourcePasswordCipher passwordCipher;

	public FmDataSourcePoolLogicServiceImpl(IFmDataSourcePoolService poolService,
			IFmTenantService tenantService, FmDataSourcePasswordCipher passwordCipher) {
		this.poolService = poolService;
		this.tenantService = tenantService;
		this.passwordCipher = passwordCipher;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataSourcePoolView> create(FmDataSourcePoolCommand command)
			throws ServiceException {
		validate(command, true);
		validateUnique(command.tenantId(), command.poolCode(), null);
		FmDataSourcePool pool = new FmDataSourcePool();
		pool.setTenantId(command.tenantId());
		pool.setPoolId(UUID.randomUUID().toString());
		pool.setPoolCode(command.poolCode().trim().toUpperCase());
		pool.setLockVersion(0);
		apply(pool, command, true);
		poolService.insert(pool);
		return load(pool.getOid(), BaseSystemMessage.insertSuccess());
	}

	@Override
	public DefaultResult<FmDataSourcePoolView> load(String oid, String message)
			throws ServiceException {
		FmDataSourcePool pool = poolService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		DefaultResult<FmDataSourcePoolView> result = success(view(pool));
		result.setMessage(message);
		return result;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataSourcePoolView> update(FmDataSourcePoolCommand command)
			throws ServiceException {
		FmDataSourcePool pool = poolService.selectByPrimaryKey(command.oid())
				.getValueEmptyThrowMessage();
		if (Boolean.FALSE.equals(Objects.equals(pool.getTenantId(), command.tenantId()))) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (Boolean.FALSE.equals(pool.getPoolCode().equalsIgnoreCase(command.poolCode()))) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (!Objects.equals(pool.getLockVersion(), command.lockVersion())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validate(command, false);
		apply(pool, command, false);
		poolService.update(pool);
		return load(pool.getOid(), BaseSystemMessage.updateSuccess());
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public DefaultResult<FmDataSourcePoolView> deactivate(String oid)
			throws ServiceException {
		FmDataSourcePool pool = poolService.selectByPrimaryKey(oid)
				.getValueEmptyThrowMessage();
		pool.setStatus("INACTIVE");
		poolService.update(pool);
		return load(oid, BaseSystemMessage.updateSuccess());
	}

	@Override
	public FmDataSourcePoolView view(FmDataSourcePool pool) {
		return new FmDataSourcePoolView(pool.getOid(), pool.getTenantId(),
				pool.getPoolId(), pool.getPoolCode(), pool.getPoolName(), pool.getDbType(),
				pool.getDriverClass(), pool.getJdbcUrl(), pool.getUsername(),
				StringUtils.isNotBlank(pool.getPasswordContent()), pool.getMaximumPoolSize(),
				pool.getMinimumIdle(), pool.getConnectionTimeoutMs(), pool.getIdleTimeoutMs(),
				pool.getMaxLifetimeMs(), pool.getValidationQuery(), pool.getStatus(),
				pool.getLockVersion(), pool.getDescription());
	}

	@Override
	public DefaultResult<List<FmOptionView>> tenantOptions() throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("status", "ACTIVE");
		return success(tenantService.selectListByParams(params, "TENANT_CODE", "ASC")
				.getValue().stream().map(tenant -> new FmOptionView(tenant.getTenantId(),
						tenant.getTenantCode() + " / " + tenant.getTenantName())).toList());
	}

	@Override
	public DefaultResult<FmDataSourceTestView> test(FmDataSourcePoolCommand command)
			throws ServiceException {
		validate(command, StringUtils.isBlank(command.oid()));
		String password = command.password();
		if (StringUtils.isBlank(password) && StringUtils.isNotBlank(command.oid())) {
			FmDataSourcePool stored = poolService.selectByPrimaryKey(command.oid())
					.getValueEmptyThrowMessage();
			if (Boolean.FALSE.equals(Objects.equals(
					stored.getTenantId(), command.tenantId()))) {
				throw new ServiceException(BaseSystemMessage.parameterIncorrect());
			}
			password = passwordCipher.decrypt(stored.getPasswordContent());
		}
		long startedAt = System.nanoTime();
		try (HikariDataSource dataSource = dataSource(command, password);
				Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
			return success(new FmDataSourceTestView(true, metadata.getDatabaseProductName(),
					metadata.getDatabaseProductVersion(), metadata.getDriverName(), elapsedMs));
		} catch (Exception exception) {
			throw new ServiceException(safeMessage(exception));
		}
	}

	private void validate(FmDataSourcePoolCommand command, boolean passwordRequired)
			throws ServiceException {
		if (command == null) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (StringUtils.isAnyBlank(command.tenantId(), command.poolCode(),
				command.poolName(), command.dbType(), command.jdbcUrl(), command.username())) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		if (passwordRequired) {
			if (StringUtils.isBlank(command.password())) {
				throw new ServiceException(BaseSystemMessage.parameterIncorrect());
			}
		}
		FmDataSourceDriverCatalog.driverClass(command.dbType());
		if (Boolean.FALSE.equals(FmDataSourceDriverCatalog.urlMatches(
				command.dbType(), command.jdbcUrl()))) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		validatePoolLimits(command);
		Map<String, Object> tenantParams = new HashMap<>();
		tenantParams.put("tenantId", command.tenantId());
		tenantParams.put("status", "ACTIVE");
		if (tenantService.selectListByParams(tenantParams, "TENANT_ID", "ASC")
				.getValue().isEmpty()) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void validatePoolLimits(FmDataSourcePoolCommand command) throws ServiceException {
		int maximumPoolSize = Objects.requireNonNullElse(command.maximumPoolSize(), 10);
		int minimumIdle = Objects.requireNonNullElse(command.minimumIdle(), 1);
		long connectionTimeout = Objects.requireNonNullElse(command.connectionTimeoutMs(), 10000L);
		boolean invalid = maximumPoolSize < 1;
		invalid = Boolean.logicalOr(invalid, maximumPoolSize > 100);
		invalid = Boolean.logicalOr(invalid, minimumIdle < 0);
		invalid = Boolean.logicalOr(invalid, minimumIdle > maximumPoolSize);
		invalid = Boolean.logicalOr(invalid, connectionTimeout < 250L);
		invalid = Boolean.logicalOr(invalid, connectionTimeout > 120000L);
		if (invalid) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void validateUnique(String tenantId, String poolCode, String excludedOid)
			throws ServiceException {
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		boolean duplicated = poolService.selectListByParams(params, "POOL_CODE", "ASC")
				.getValue().stream()
				.filter(pool -> Boolean.FALSE.equals(Objects.equals(pool.getOid(), excludedOid)))
				.anyMatch(pool -> pool.getPoolCode().equalsIgnoreCase(poolCode.trim()));
		if (duplicated) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private void apply(FmDataSourcePool pool, FmDataSourcePoolCommand command,
			boolean passwordRequired) throws ServiceException {
		pool.setPoolName(command.poolName().trim());
		pool.setDbType(command.dbType());
		pool.setDriverClass(FmDataSourceDriverCatalog.driverClass(command.dbType()));
		pool.setJdbcUrl(command.jdbcUrl().trim());
		pool.setUsername(command.username().trim());
		if (StringUtils.isNotBlank(command.password())) {
			pool.setPasswordContent(passwordCipher.encrypt(command.password()));
		} else if (passwordRequired) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
		pool.setMaximumPoolSize(Objects.requireNonNullElse(command.maximumPoolSize(), 10));
		pool.setMinimumIdle(Objects.requireNonNullElse(command.minimumIdle(), 1));
		pool.setConnectionTimeoutMs(
				Objects.requireNonNullElse(command.connectionTimeoutMs(), 10000L));
		pool.setIdleTimeoutMs(Objects.requireNonNullElse(command.idleTimeoutMs(), 600000L));
		pool.setMaxLifetimeMs(Objects.requireNonNullElse(command.maxLifetimeMs(), 1800000L));
		pool.setValidationQuery(StringUtils.trimToNull(command.validationQuery()));
		pool.setStatus(StringUtils.defaultIfBlank(command.status(), "ACTIVE"));
		pool.setDescription(StringUtils.trimToNull(command.description()));
	}

	private HikariDataSource dataSource(FmDataSourcePoolCommand command, String password)
			throws ServiceException {
		HikariConfig config = new HikariConfig();
		config.setPoolName("fm-test-" + UUID.randomUUID());
		config.setDriverClassName(FmDataSourceDriverCatalog.driverClass(command.dbType()));
		config.setJdbcUrl(command.jdbcUrl());
		config.setUsername(command.username());
		config.setPassword(password);
		config.setMaximumPoolSize(1);
		config.setMinimumIdle(0);
		config.setConnectionTimeout(
				Objects.requireNonNullElse(command.connectionTimeoutMs(), 10000L));
		config.setInitializationFailTimeout(-1L);
		if (StringUtils.isNotBlank(command.validationQuery())) {
			config.setConnectionTestQuery(command.validationQuery());
		}
		return new HikariDataSource(config);
	}

	private String safeMessage(Exception exception) {
		String message = StringUtils.defaultIfBlank(exception.getMessage(),
				exception.getClass().getSimpleName());
		return StringUtils.abbreviate(message, 300);
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
