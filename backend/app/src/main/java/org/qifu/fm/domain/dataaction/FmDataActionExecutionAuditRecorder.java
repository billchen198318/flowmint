package org.qifu.fm.domain.dataaction;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.qifu.fm.entity.FmDataAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FmDataActionExecutionAuditRecorder {

	private static final Logger LOGGER = LoggerFactory.getLogger(
			FmDataActionExecutionAuditRecorder.class);
	private static final String INSERT_SQL = """
			INSERT INTO fm_data_action_execution_audit
			    (OID, EXECUTION_ID, TENANT_ID, ACTION_ID, ACTION_CODE, VERSION_NO,
			     LOGIN_ACCOUNT, EXECUTION_STATUS, ROLLBACK_ONLY, STEP_COUNT,
			     REQUEST_PARAMETER_COUNT, START_TIME, END_TIME, DURATION_MS,
			     ERROR_MESSAGE, CDATE)
			VALUES
			    (:oid, :executionId, :tenantId, :actionId, :actionCode, :versionNo,
			     :loginAccount, :executionStatus, :rollbackOnly, :stepCount,
			     :requestParameterCount, :startTime, :endTime, :durationMs,
			     :errorMessage, :cdate)
			""";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public FmDataActionExecutionAuditRecorder(
			@Qualifier("db1JdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void record(String executionId, FmDataAction action, Integer versionNo,
			String loginAccount, boolean rollbackOnly, int stepCount,
			int requestParameterCount, Instant startedAt, Throwable failure) {
		Instant endedAt = Instant.now();
		String errorMessage = failure == null ? null : abbreviate(failure.getMessage());
		Map<String, Object> parameters = Map.ofEntries(
				Map.entry("oid", java.util.UUID.randomUUID().toString()),
				Map.entry("executionId", executionId),
				Map.entry("tenantId", action.getTenantId()),
				Map.entry("actionId", action.getActionId()),
				Map.entry("actionCode", action.getActionCode()),
				Map.entry("versionNo", versionNo),
				Map.entry("loginAccount", loginAccount),
				Map.entry("executionStatus", failure == null ? "SUCCESS" : "FAILED"),
				Map.entry("rollbackOnly", rollbackOnly ? "Y" : "N"),
				Map.entry("stepCount", stepCount),
				Map.entry("requestParameterCount", requestParameterCount),
				Map.entry("startTime", Date.from(startedAt)),
				Map.entry("endTime", Date.from(endedAt)),
				Map.entry("durationMs", Math.max(0L, endedAt.toEpochMilli()
						- startedAt.toEpochMilli())),
				Map.entry("errorMessage", errorMessage == null ? "" : errorMessage),
				Map.entry("cdate", Date.from(endedAt)));
		try {
			jdbcTemplate.update(INSERT_SQL, parameters);
		} catch (Exception exception) {
			LOGGER.error("Data Action execution audit 寫入失敗，executionId={}",
					executionId, exception);
		}
	}

	private String abbreviate(String value) {
		if (value == null) {
			return "執行失敗";
		}
		return value.length() <= 1000 ? value : value.substring(0, 1000);
	}
}
