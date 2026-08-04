package org.qifu.fm.domain.dataaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.datasource.FmDataSourcePoolRegistry;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class FmDataActionExecutor {

	private final FmDataSourcePoolRegistry poolRegistry;
	private final FmDataActionParameterResolver parameterResolver;
	private final FmDataActionSqlValidator sqlValidator;

	public FmDataActionExecutor(FmDataSourcePoolRegistry poolRegistry,
			FmDataActionParameterResolver parameterResolver,
			FmDataActionSqlValidator sqlValidator) {
		this.poolRegistry = poolRegistry;
		this.parameterResolver = parameterResolver;
		this.sqlValidator = sqlValidator;
	}

	public FmDataActionExecutionView execute(FmDataAction action,
			Integer versionNo, List<FmDataActionStep> steps,
			Map<String, Object> request, String loginAccount,
			boolean rollback) throws ServiceException {
		Map<String, Object> parameters = parameterResolver.resolve(
				action.getRequestSchema(), request, action.getTenantId(), loginAccount);
		validateSteps(action, steps, parameters.keySet());
		DataSource dataSource = poolRegistry.get(
				action.getTenantId(), action.getPoolId());
		NamedParameterJdbcTemplate jdbcTemplate =
				new NamedParameterJdbcTemplate(dataSource);
		String executionId = UUID.randomUUID().toString();
		Map<String, Object> data;
		if ("TRANSACTION".equals(action.getActionType()) || rollback) {
			data = executeInTransaction(dataSource, jdbcTemplate,
					steps, parameters, rollback);
		} else {
			data = executeSteps(jdbcTemplate, steps, parameters);
		}
		return new FmDataActionExecutionView(executionId,
				action.getActionCode(), versionNo, rollback, data);
	}

	private Map<String, Object> executeInTransaction(DataSource dataSource,
			NamedParameterJdbcTemplate jdbcTemplate,
			List<FmDataActionStep> steps, Map<String, Object> parameters,
			boolean rollback) throws ServiceException {
		PlatformTransactionManager transactionManager =
				new org.springframework.jdbc.datasource.DataSourceTransactionManager(
						dataSource);
		TransactionTemplate transactionTemplate =
				new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(
				TransactionDefinition.PROPAGATION_REQUIRED);
		try {
			return transactionTemplate.execute(status -> {
				try {
					Map<String, Object> result = executeSteps(
							jdbcTemplate, steps, parameters);
					if (rollback) {
						status.setRollbackOnly();
					}
					return result;
				} catch (ServiceException exception) {
					throw new DataActionRuntimeException(exception);
				}
			});
		} catch (DataActionRuntimeException exception) {
			throw exception.getServiceException();
		}
	}

	private Map<String, Object> executeSteps(
			NamedParameterJdbcTemplate jdbcTemplate,
			List<FmDataActionStep> steps, Map<String, Object> parameters)
			throws ServiceException {
		Map<String, Object> data = new LinkedHashMap<>();
		for (FmDataActionStep step : steps) {
			if (!"ACTIVE".equals(step.getStatus())) {
				continue;
			}
			try {
				Object result = executeStep(jdbcTemplate, step, parameters);
				if (!"NONE".equals(step.getResultMode())) {
					data.put(step.getResultKey(), result);
				}
			} catch (ServiceException exception) {
				throw exception;
			} catch (Exception exception) {
				throw new ServiceException("Data Action Step 執行失敗："
						+ step.getStepCode());
			}
		}
		return data;
	}

	private Object executeStep(NamedParameterJdbcTemplate jdbcTemplate,
			FmDataActionStep step, Map<String, Object> parameters)
			throws ServiceException {
		jdbcTemplate.getJdbcTemplate().setQueryTimeout(
				step.getQueryTimeoutSeconds() == null
						? 30 : step.getQueryTimeoutSeconds());
		jdbcTemplate.getJdbcTemplate().setMaxRows(
				step.getMaxRows() == null ? 1000 : step.getMaxRows() + 1);
		String statementType = step.getStatementType();
		if ("SELECT_ONE".equals(statementType)) {
			List<Map<String, Object>> rows = normalizeRows(jdbcTemplate.queryForList(
					step.getSqlContent(), parameters), step.getMaxRows());
			if (rows.size() > 1) {
				throw new ServiceException("SELECT_ONE 回傳超過一筆資料");
			}
			return rows.isEmpty() ? null : rows.get(0);
		}
		if ("SELECT_LIST".equals(statementType)) {
			return normalizeRows(jdbcTemplate.queryForList(
					step.getSqlContent(), parameters), step.getMaxRows());
		}
		int affectedRows = jdbcTemplate.update(step.getSqlContent(), parameters);
		if (step.getExpectAffectedRows() != null
				&& step.getExpectAffectedRows() != affectedRows) {
			throw new ServiceException("Affected Rows 不符合預期："
					+ step.getStepCode());
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("affectedRows", affectedRows);
		return result;
	}

	private List<Map<String, Object>> normalizeRows(
			List<Map<String, Object>> source, Integer configuredMaxRows)
			throws ServiceException {
		int maxRows = configuredMaxRows == null ? 1000 : configuredMaxRows;
		if (source.size() > maxRows) {
			throw new ServiceException("查詢結果超過最大回傳筆數");
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map<String, Object> sourceRow : source) {
			Map<String, Object> row = new LinkedHashMap<>();
			sourceRow.forEach((key, value) -> row.put(toLowerCamel(key), value));
			rows.add(row);
		}
		return rows;
	}

	private void validateSteps(FmDataAction action,
			List<FmDataActionStep> steps, Set<String> availableParameters)
			throws ServiceException {
		if (steps.isEmpty()) {
			throw new ServiceException("Data Action 至少需要一個 SQL Step");
		}
		for (FmDataActionStep step : steps) {
			sqlValidator.validate(step, action.getActionType(), availableParameters);
		}
	}

	private String toLowerCamel(String value) {
		String[] segments = value.toLowerCase(Locale.ROOT).split("_");
		StringBuilder result = new StringBuilder(segments[0]);
		for (int index = 1; index < segments.length; index++) {
			if (!segments[index].isEmpty()) {
				result.append(Character.toUpperCase(segments[index].charAt(0)))
						.append(segments[index].substring(1));
			}
		}
		return result.toString();
	}

	private static final class DataActionRuntimeException
			extends RuntimeException {

		private static final long serialVersionUID = 1L;
		private final ServiceException serviceException;

		private DataActionRuntimeException(ServiceException serviceException) {
			super(serviceException);
			this.serviceException = serviceException;
		}

		private ServiceException getServiceException() {
			return serviceException;
		}
	}
}
