package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class FmDataActionRateLimiterTest {

	@Test
	void allowsRequestsBelowLimitAndRejectsAtLimit() {
		NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
		when(jdbc.queryForObject(contains("fm_data_action_execution_audit"),
				anyMap(), org.mockito.ArgumentMatchers.eq(Long.class)))
				.thenReturn(4L, 5L);
		FmDataActionRateLimiter limiter = new FmDataActionRateLimiter(jdbc, 5);
		org.qifu.fm.entity.FmDataAction action = new org.qifu.fm.entity.FmDataAction();
		action.setTenantId("A01"); action.setActionId("ACTION-1");

		assertDoesNotThrow(() -> limiter.check(action, "user01"));
		assertThrows(ServiceException.class,
				() -> limiter.check(action, "user01"));
	}

	@Test
	void actionOverrideReplacesGlobalLimit() {
		NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
		when(jdbc.queryForObject(contains("fm_data_action_execution_audit"),
				anyMap(), org.mockito.ArgumentMatchers.eq(Long.class))).thenReturn(2L);
		FmDataActionRateLimiter limiter = new FmDataActionRateLimiter(jdbc, 60);
		org.qifu.fm.entity.FmDataAction action = new org.qifu.fm.entity.FmDataAction();
		action.setTenantId("A01"); action.setActionId("ACTION-1");
		action.setRateLimitPerMinute(2);

		assertThrows(ServiceException.class, () -> limiter.check(action, "user01"));
	}
}
