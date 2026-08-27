package org.qifu.fm.domain.dataaction;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmDataAction;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class FmDataActionExecutionAuditRecorderTest {

	@Test
	void recordsExecutionMetadataWithoutRequestContent() {
		NamedParameterJdbcTemplate jdbcTemplate =
				mock(NamedParameterJdbcTemplate.class);
		when(jdbcTemplate.update(contains("fm_data_action_execution_audit"),
				anyMap())).thenReturn(1);
		FmDataAction action = new FmDataAction();
		action.setTenantId("A01");
		action.setActionId("ACTION-1");
		action.setActionCode("TEST_ACTION");

		new FmDataActionExecutionAuditRecorder(jdbcTemplate).record(
				"EXEC-1", action, 2, "user01", false,
				3, 5, Instant.now(), null);

		verify(jdbcTemplate).update(
				contains("fm_data_action_execution_audit"), anyMap());
	}

	@Test
	void auditFailureDoesNotReplaceBusinessResult() {
		NamedParameterJdbcTemplate jdbcTemplate =
				mock(NamedParameterJdbcTemplate.class);
		when(jdbcTemplate.update(contains("fm_data_action_execution_audit"),
				anyMap())).thenThrow(new IllegalStateException("table unavailable"));
		FmDataAction action = new FmDataAction();
		action.setTenantId("A01");
		action.setActionId("ACTION-1");
		action.setActionCode("TEST_ACTION");

		new FmDataActionExecutionAuditRecorder(jdbcTemplate).record(
				"EXEC-2", action, 2, "user01", false,
				1, 2, Instant.now(), new IllegalArgumentException("failed"));
	}
}
