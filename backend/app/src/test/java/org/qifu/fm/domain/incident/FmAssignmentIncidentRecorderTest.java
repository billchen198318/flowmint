package org.qifu.fm.domain.incident;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class FmAssignmentIncidentRecorderTest {

    @Test
    void insertsOpenTenantScopedIncidentAndTruncatesDatabaseMessage() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        FmAssignmentIncidentRecorder recorder = new FmAssignmentIncidentRecorder(jdbc);
        recorder.record(new FmAssignmentIncidentRecorder.IncidentCommand(
                "T1", "P1", "TASK1", "manager", "ASSIGNMENT",
                "RESOLVER_FAILED", "x".repeat(2100), "{}", "system", new Date()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(Map.class);
        verify(jdbc).update(contains("'OPEN'"), parameters.capture());
        assertEquals("T1", parameters.getValue().get("tenantId"));
        assertEquals(2000, parameters.getValue().get("errorMessage").toString().length());
        verify(jdbc).update(contains("fm_assignment_incident"), anyMap());
    }
}
