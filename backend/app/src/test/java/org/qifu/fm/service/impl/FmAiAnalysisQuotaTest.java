package org.qifu.fm.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmAiAnalysis;
import org.qifu.fm.mapper.FmAiAnalysisMapper;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class FmAiAnalysisQuotaTest {
	@Test
	void rejectsActorDailyQuotaBeforeStartingAnalysis() throws Exception {
		var fixture = fixture();
		when(fixture.jdbc.queryForMap(anyString(), anyMap()))
				.thenReturn(counts(0, 0, 0), counts(0, 20, 0));
		assertThrows(ServiceException.class, () -> fixture.service.start(analysis("N")));
		verify(fixture.mapper, never()).insert(any(FmAiAnalysis.class));
	}

	@Test
	void rejectsForceRefreshDuringCooldown() throws Exception {
		var fixture = fixture();
		when(fixture.jdbc.queryForMap(anyString(), anyMap()))
				.thenReturn(counts(0, 0, 0));
		when(fixture.jdbc.queryForObject(anyString(), anyMap(), eq(Date.class)))
				.thenReturn(new Date());
		assertThrows(ServiceException.class, () -> fixture.service.start(analysis("Y")));
		verifyNoInteractions(fixture.mapper);
	}

	@Test
	void rejectsForceRefreshDailyQuota() throws Exception {
		var fixture = fixture();
		when(fixture.jdbc.queryForMap(anyString(), anyMap()))
				.thenReturn(counts(0, 0, 0), counts(0, 0, 0), counts(0, 5, 0));
		assertThrows(ServiceException.class, () -> fixture.service.start(analysis("Y")));
		verifyNoInteractions(fixture.mapper);
	}

	private Fixture fixture() {
		FmAiAnalysisMapper mapper = mock(FmAiAnalysisMapper.class);
		NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
		Environment environment = mock(Environment.class);
		when(environment.getProperty("fm.ai.quota-zone", "Asia/Taipei"))
				.thenReturn("Asia/Taipei");
		when(environment.getProperty(anyString(), eq(Integer.class), any(Integer.class)))
				.thenAnswer(call -> call.getArgument(2));
		when(jdbc.query(anyString(), anyMap(), any(ResultSetExtractor.class)))
				.thenReturn("TENANT");
		when(jdbc.queryForObject(anyString(), anyMap(), eq(Long.class)))
				.thenReturn(0L);
		return new Fixture(new FmAiAnalysisServiceImpl(mapper, jdbc, environment),
				mapper, jdbc);
	}

	private FmAiAnalysis analysis(String force) {
		FmAiAnalysis value = new FmAiAnalysis();
		value.setTenantId("TENANT");
		value.setTaskId("TASK");
		value.setActorAccount("actor");
		value.setContentHash("hash");
		value.setForceRefreshFlag(force);
		return value;
	}

	private Map<String, Object> counts(long tenant, long actor, long task) {
		return Map.of("tenant_count", tenant, "actor_count", actor, "task_count", task);
	}

	private record Fixture(FmAiAnalysisServiceImpl service,
			FmAiAnalysisMapper mapper, NamedParameterJdbcTemplate jdbc) {
	}
}
