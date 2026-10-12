package org.qifu.fm.service.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAiAnalysis;
import org.qifu.fm.mapper.FmAiAnalysisMapper;
import org.qifu.fm.service.IFmAiAnalysisService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAiAnalysisServiceImpl extends BaseService<FmAiAnalysis, String>
		implements IFmAiAnalysisService {
	private static final String LOCK_TENANT_SQL = """
			SELECT TENANT_ID FROM fm_tenant
			 WHERE TENANT_ID=:tenantId AND STATUS='ACTIVE' FOR UPDATE
			""";
	private static final String USAGE_SQL = """
			SELECT COUNT(*) AS tenant_count,
			       COALESCE(SUM(CASE WHEN ACTOR_ACCOUNT=:actor THEN 1 ELSE 0 END),0) AS actor_count,
			       COALESCE(SUM(CASE WHEN TASK_ID=:taskId THEN 1 ELSE 0 END),0) AS task_count
			  FROM fm_ai_analysis
			 WHERE TENANT_ID=:tenantId AND START_DATE>=:since
			   AND (:forceOnly='N' OR FORCE_REFRESH_FLAG='Y')
			""";
	private static final String LAST_REFRESH_SQL = """
			SELECT MAX(START_DATE) FROM fm_ai_analysis
			 WHERE TENANT_ID=:tenantId AND CONTENT_HASH=:contentHash
			   AND FORCE_REFRESH_FLAG='Y'
			""";
	private static final String RUNNING_SQL = """
			SELECT COUNT(*) FROM fm_ai_analysis
			 WHERE TENANT_ID=:tenantId AND CONTENT_HASH=:contentHash
			   AND EXECUTION_STATUS IN ('PENDING','RUNNING')
			""";

	private final FmAiAnalysisMapper mapper;
	private final NamedParameterJdbcTemplate jdbc;
	private final Environment environment;

	public FmAiAnalysisServiceImpl(FmAiAnalysisMapper mapper,
			@Qualifier("db1JdbcTemplate") NamedParameterJdbcTemplate jdbc,
			Environment environment) {
		this.mapper = mapper;
		this.jdbc = jdbc;
		this.environment = environment;
	}

	@Override
	protected IBaseMapper<FmAiAnalysis, String> getBaseMapper() {
		return mapper;
	}

	@Override
	public FmAiAnalysis findLatestSucceeded(String tenantId, String contentHash) {
		return mapper.findLatestSucceeded(parameters(tenantId, contentHash));
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void start(FmAiAnalysis analysis) throws ServiceException {
		Map<String, Object> identity = parameters(analysis.getTenantId(), analysis.getContentHash());
		String tenant = jdbc.query(LOCK_TENANT_SQL, identity, result ->
				result.next() ? result.getString(1) : null);
		if (tenant == null) {
			throw new ServiceException("AI 分析的 Tenant 不存在或已停用");
		}
		Long running = jdbc.queryForObject(RUNNING_SQL, identity, Long.class);
		if (running != null && running > 0) {
			throw new ServiceException("AI 分析正在執行，請稍後再試");
		}
		if (!"Y".equals(analysis.getForceRefreshFlag())
				&& mapper.findLatestSucceeded(identity) != null) {
			throw new ServiceException("AI 分析已完成，請重新取得快取");
		}
		checkQuota(analysis);
		Integer generation = mapper.findNextGenerationNo(identity);
		analysis.setGenerationNo(generation == null ? 1 : generation);
		insert(analysis);
	}

	private void checkQuota(FmAiAnalysis analysis) throws ServiceException {
		Instant now = Instant.now();
		ZoneId zone = ZoneId.of(environment.getProperty("fm.ai.quota-zone", "Asia/Taipei"));
		Date minuteStart = Date.from(now.minusSeconds(60));
		Date dayStart = Date.from(now.atZone(zone).toLocalDate()
				.atStartOfDay(zone).toInstant());
		Map<String, Object> minute = usage(analysis, minuteStart, false);
		Map<String, Object> daily = usage(analysis, dayStart, false);
		requireBelow(minute, "actor_count", limit("fm.ai.actor-per-minute", 3));
		requireBelow(minute, "task_count", limit("fm.ai.task-per-minute", 5));
		requireBelow(minute, "tenant_count", limit("fm.ai.tenant-per-minute", 60));
		requireBelow(daily, "actor_count", limit("fm.ai.actor-per-day", 20));
		requireBelow(daily, "task_count", limit("fm.ai.task-per-day", 50));
		requireBelow(daily, "tenant_count", limit("fm.ai.tenant-per-day", 500));
		if ("Y".equals(analysis.getForceRefreshFlag())) {
			Map<String, Object> identity = parameters(analysis.getTenantId(), analysis.getContentHash());
			Date last = jdbc.queryForObject(LAST_REFRESH_SQL, identity, Date.class);
			if (last != null && last.toInstant().isAfter(now.minusSeconds(
					limit("fm.ai.force-refresh-cooldown-seconds", 300)))) {
				throw new ServiceException("AI 強制重新分析冷卻中，請稍後再試");
			}
			Map<String, Object> forcedDaily = usage(analysis, dayStart, true);
			requireBelow(forcedDaily, "actor_count", limit("fm.ai.force-actor-per-day", 5));
			requireBelow(forcedDaily, "task_count", limit("fm.ai.force-task-per-day", 10));
			requireBelow(forcedDaily, "tenant_count", limit("fm.ai.force-tenant-per-day", 100));
		}
	}

	private Map<String, Object> usage(FmAiAnalysis analysis, Date since, boolean forceOnly) {
		Map<String, Object> params = parameters(analysis.getTenantId(), analysis.getContentHash());
		params.put("actor", analysis.getActorAccount());
		params.put("taskId", analysis.getTaskId());
		params.put("since", since);
		params.put("forceOnly", forceOnly ? "Y" : "N");
		return jdbc.queryForMap(USAGE_SQL, params);
	}

	private int limit(String key, int fallback) {
		return Math.max(1, environment.getProperty(key, Integer.class, fallback));
	}

	private void requireBelow(Map<String, Object> usage, String counter, int limit)
			throws ServiceException {
		if (((Number) usage.get(counter)).longValue() >= limit) {
			throw new ServiceException("AI 分析呼叫額度已滿，請稍後再試");
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void complete(FmAiAnalysis analysis) throws ServiceException {
		if (mapper.complete(statusParameters(analysis)) != 1) {
			throw new ServiceException("AI Analysis 狀態已變更，無法重複完成");
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void fail(FmAiAnalysis analysis) throws ServiceException {
		if (mapper.fail(statusParameters(analysis)) != 1) {
			throw new ServiceException("AI Analysis 狀態已變更，無法重複標記失敗");
		}
	}

	private Map<String, Object> parameters(String tenantId, String contentHash) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("contentHash", contentHash);
		return parameters;
	}

	private Map<String, Object> statusParameters(FmAiAnalysis analysis) {
		Map<String, Object> parameters = parameters(
				analysis.getTenantId(), analysis.getContentHash());
		parameters.put("analysisId", analysis.getAnalysisId());
		parameters.put("completeDate", analysis.getCompleteDate());
		parameters.put("elapsedMillis", analysis.getElapsedMillis());
		parameters.put("inputTokens", analysis.getInputTokens());
		parameters.put("outputTokens", analysis.getOutputTokens());
		parameters.put("resultContent", analysis.getResultContent());
		parameters.put("errorCode", analysis.getErrorCode());
		return parameters;
	}
}
