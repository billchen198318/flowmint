package org.qifu.fm.domain.dataaction;

import java.util.ArrayList;
import java.time.Instant;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class FmDataActionExecutor {

	private final FmDataSourcePoolRegistry poolRegistry;
	private final FmDataActionParameterResolver parameterResolver;
	private final FmDataActionSqlValidator sqlValidator;
	private final FmDataActionExecutionAuditRecorder auditRecorder;
	private final FmDataActionRateLimiter rateLimiter;
	private final FmDataActionContinueConditionEvaluator conditionEvaluator;

	public FmDataActionExecutor(FmDataSourcePoolRegistry poolRegistry,
			FmDataActionParameterResolver parameterResolver,
			FmDataActionSqlValidator sqlValidator,
			FmDataActionExecutionAuditRecorder auditRecorder,
			FmDataActionRateLimiter rateLimiter,
			FmDataActionContinueConditionEvaluator conditionEvaluator) {
		this.poolRegistry = poolRegistry;
		this.parameterResolver = parameterResolver;
		this.sqlValidator = sqlValidator;
		this.auditRecorder = auditRecorder;
		this.rateLimiter = rateLimiter;
		this.conditionEvaluator = conditionEvaluator;
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
		Instant startedAt = Instant.now();
		Throwable failure = null;
		try {
			rateLimiter.check(action, loginAccount);
			Map<String, Object> data;
			if ("TRANSACTION".equals(action.getActionType()) || rollback) {
				data = executeInTransaction(dataSource, jdbcTemplate,
						steps, parameters, request, action.getRequestSchema(), rollback);
			} else {
				data = executeSteps(jdbcTemplate, steps, parameters,
						request, action.getRequestSchema());
			}
			return new FmDataActionExecutionView(executionId,
					action.getActionCode(), versionNo, rollback, data);
		} catch (RuntimeException exception) {
			failure = exception;
			throw exception;
		} finally {
			auditRecorder.record(executionId, action, versionNo, loginAccount,
					rollback, steps.size(), parameters.size(), startedAt, failure);
		}
	}

	private Map<String, Object> executeInTransaction(DataSource dataSource,
			NamedParameterJdbcTemplate jdbcTemplate,
			List<FmDataActionStep> steps, Map<String, Object> parameters,
			Map<String, Object> request, String requestSchema,
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
							jdbcTemplate, steps, parameters, request, requestSchema);
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
			List<FmDataActionStep> steps, Map<String, Object> parameters,
			Map<String, Object> request, String requestSchema)
			throws ServiceException {
		Map<String, Object> data = new LinkedHashMap<>();
		for (FmDataActionStep step : steps) {
			if (!"ACTIVE".equals(step.getStatus())) {
				continue;
			}
			try {
				if (!conditionEvaluator.evaluate(step.getContinueCondition(), request, data)) {
					continue;
				}
				Object result;
				if ("FOR_EACH".equals(step.getExecutionMode())) {
					result = executeForEach(jdbcTemplate, step, parameters,
							request, requestSchema, data);
				} else {
					Map<String, Object> stepParameters = parameterResolver.resolveForStep(
							requestSchema, parameters, request, data, null);
					result = executeStep(jdbcTemplate, step, stepParameters);
				}
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

	private Object executeForEach(NamedParameterJdbcTemplate jdbcTemplate,
			FmDataActionStep step, Map<String, Object> baseParameters,
			Map<String, Object> request, String requestSchema,
			Map<String, Object> stepResults) throws ServiceException {
		Object source = parameterResolver.readRequestPath(request, step.getArrayPath());
		if (!(source instanceof List<?> items)) {
			throw new ServiceException("FOR_EACH Array Path 不是陣列："
					+ step.getStepCode());
		}
		int maxItems = step.getMaxRows() == null ? 1000 : step.getMaxRows();
		if (items.size() > maxItems) {
			throw new ServiceException("FOR_EACH 筆數超過上限：" + step.getStepCode());
		}
		List<Map<String, Object>> batchParameters = new ArrayList<>();
		for (Object item : items) {
			batchParameters.add(parameterResolver.resolveForStep(requestSchema,
					baseParameters, request, stepResults, item));
		}
		if ("GENERATED_KEY".equals(step.getResultMode())) {
			List<Object> keys = new ArrayList<>();
			int affectedRows = 0;
			for (Map<String, Object> itemParameters : batchParameters) {
				Map<String, Object> result = executeMutation(
						jdbcTemplate, step, itemParameters, true);
				affectedRows += (Integer) result.get("affectedRows");
				keys.add(result.get("generatedKey"));
			}
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("affectedRows", affectedRows);
			result.put("generatedKeys", keys);
			return result;
		}
		SqlParameterSource[] batch = batchParameters.stream()
				.map(MapSqlParameterSource::new)
				.toArray(SqlParameterSource[]::new);
		int[] counts = jdbcTemplate.batchUpdate(step.getSqlContent(), batch);
		int affectedRows = 0;
		for (int index = 0; index < counts.length; index++) {
			int count = counts[index];
			if (step.getExpectAffectedRows() != null
					&& step.getExpectAffectedRows() != count) {
				throw new ServiceException("Affected Rows 不符合預期："
						+ step.getStepCode() + "，索引 " + index);
			}
			affectedRows += Math.max(count, 0);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("affectedRows", affectedRows);
		result.put("batchSize", items.size());
		return result;
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
			if (parameters.containsKey("page") || parameters.containsKey("pageSize")) {
				return executePagedList(jdbcTemplate, step, parameters);
			}
			return normalizeRows(jdbcTemplate.queryForList(
					step.getSqlContent(), parameters), step.getMaxRows());
		}
		return executeMutation(jdbcTemplate, step, parameters,
				"GENERATED_KEY".equals(step.getResultMode()));
	}

	private Map<String, Object> executePagedList(
			NamedParameterJdbcTemplate jdbcTemplate, FmDataActionStep step,
			Map<String, Object> parameters) throws ServiceException {
		if (!(parameters.get("page") instanceof Integer page)
				|| !(parameters.get("pageSize") instanceof Integer pageSize)) {
			throw new ServiceException("分頁查詢必須同時提供 page 與 pageSize");
		}
		int maxPageSize = step.getMaxRows() == null ? 1000 : step.getMaxRows();
		if (pageSize > maxPageSize) {
			throw new ServiceException("pageSize 超過 Step 最大回傳筆數");
		}
		long offsetValue = (long) (page - 1) * pageSize;
		if (offsetValue > Integer.MAX_VALUE - pageSize) {
			throw new ServiceException("分頁範圍過大");
		}
		String countSql = "SELECT COUNT(*) FROM ("
				+ stripTopLevelOrderBy(step.getSqlContent())
				+ ") FM_DATA_ACTION_COUNT";
		Long total = jdbcTemplate.queryForObject(countSql, parameters, Long.class);
		jdbcTemplate.getJdbcTemplate().setMaxRows((int) offsetValue + pageSize);
		List<Map<String, Object>> source = jdbcTemplate.queryForList(
				step.getSqlContent(), parameters);
		int from = Math.min((int) offsetValue, source.size());
		int to = Math.min(from + pageSize, source.size());
		List<Map<String, Object>> items = normalizeRows(
				new ArrayList<>(source.subList(from, to)), pageSize);
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("items", items);
		result.put("page", page);
		result.put("pageSize", pageSize);
		result.put("total", total == null ? 0L : total);
		result.put("totalPages", total == null ? 0L
				: (total + pageSize - 1) / pageSize);
		return result;
	}

	static String stripTopLevelOrderBy(String sql) {
		String upper = sql.toUpperCase(Locale.ROOT);
		int depth = 0;
		boolean quoted = false;
		for (int index = 0; index < upper.length() - 8; index++) {
			char current = upper.charAt(index);
			if (current == '\'') {
				quoted = !quoted;
			} else if (!quoted && current == '(') {
				depth++;
			} else if (!quoted && current == ')') {
				depth--;
			} else if (!quoted && depth == 0
					&& upper.startsWith("ORDER BY", index)
					&& (index == 0 || Character.isWhitespace(upper.charAt(index - 1)))) {
				return sql.substring(0, index).trim();
			}
		}
		return sql;
	}

	private Map<String, Object> executeMutation(
			NamedParameterJdbcTemplate jdbcTemplate, FmDataActionStep step,
			Map<String, Object> parameters, boolean returnGeneratedKey)
			throws ServiceException {
		int affectedRows;
		KeyHolder keyHolder = null;
		if (returnGeneratedKey) {
			keyHolder = new GeneratedKeyHolder();
			affectedRows = jdbcTemplate.update(step.getSqlContent(),
					new MapSqlParameterSource(parameters), keyHolder);
		} else {
			affectedRows = jdbcTemplate.update(step.getSqlContent(), parameters);
		}
		if (step.getExpectAffectedRows() != null
				&& step.getExpectAffectedRows() != affectedRows) {
			throw new ServiceException("Affected Rows 不符合預期："
					+ step.getStepCode());
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("affectedRows", affectedRows);
		if (returnGeneratedKey) {
			Number key = keyHolder.getKey();
			if (key == null) {
				throw new ServiceException("資料庫未回傳 Generated Key："
						+ step.getStepCode());
			}
			result.put("generatedKey", key);
		}
		return result;
	}


	static List<Map<String, Object>> normalizeRows(
			List<Map<String, Object>> source, Integer configuredMaxRows)
			throws ServiceException {
		int maxRows = configuredMaxRows == null ? 1000 : configuredMaxRows;
		if (source.size() > maxRows) {
			throw new ServiceException("查詢結果超過最大回傳筆數");
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Map<String, Object> sourceRow : source) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (Map.Entry<String, Object> column : sourceRow.entrySet()) {
				String normalized = toLowerCamel(column.getKey());
				if (row.containsKey(normalized)) {
					throw new ServiceException(
							"Data Action 回傳欄位正規化後重複：" + normalized);
				}
				row.put(normalized, column.getValue());
			}
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

	static String toLowerCamel(String value) {
		if (!value.contains("_")) {
			boolean mixedCase = value.chars().anyMatch(Character::isLowerCase)
					&& value.chars().anyMatch(Character::isUpperCase);
			return mixedCase
					? Character.toLowerCase(value.charAt(0)) + value.substring(1)
					: value.toLowerCase(Locale.ROOT);
		}
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
