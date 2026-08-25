package org.qifu.fm.domain.dataaction;

import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmDataAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FmDataActionRateLimiter {

	private static final String COUNT_SQL = """
			SELECT COUNT(*)
			  FROM fm_data_action_execution_audit
			 WHERE TENANT_ID=:tenantId
			   AND ACTION_ID=:actionId
			   AND LOGIN_ACCOUNT=:loginAccount
			   AND START_TIME >= DATE_SUB(NOW(3), INTERVAL 1 MINUTE)
			""";

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final int requestsPerMinute;

	public FmDataActionRateLimiter(
			@Qualifier("db1JdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate,
			@Value("${fm.data-action.rate-limit-per-minute:60}") int requestsPerMinute) {
		this.jdbcTemplate = jdbcTemplate;
		this.requestsPerMinute = Math.max(1, requestsPerMinute);
	}

	public void check(FmDataAction action, String loginAccount)
			throws ServiceException {
		int effectiveLimit = action.getRateLimitPerMinute() == null
				? requestsPerMinute : action.getRateLimitPerMinute();
		Long count = jdbcTemplate.queryForObject(COUNT_SQL,
				Map.of("tenantId", action.getTenantId(), "actionId", action.getActionId(),
						"loginAccount", loginAccount), Long.class);
		if (count != null && count >= effectiveLimit) {
			throw new ServiceException("Data Action 呼叫過於頻繁，請稍後再試");
		}
	}
}
