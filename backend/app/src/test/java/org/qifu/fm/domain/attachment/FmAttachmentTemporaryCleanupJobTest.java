package org.qifu.fm.domain.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

class FmAttachmentTemporaryCleanupJobTest {

    @Test
    void cleanupLocksExpiredOpenSessionBeforeReadingFiles() {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        FmAttachmentTemporaryCleanupJob cleanup = new FmAttachmentTemporaryCleanupJob(
                jdbc, mock(FmAttachmentStorageService.class));
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", "A01").addValue("sessionId", "batch");
        when(jdbc.queryForList(anyString(), any(MapSqlParameterSource.class),
                eq(String.class))).thenReturn(List.of());
        assertFalse(cleanup.lockExpiredOpenSession(parameters));
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForList(sql.capture(), any(MapSqlParameterSource.class),
                eq(String.class));
        assertTrue(sql.getValue().contains("SESSION_STATUS = 'OPEN'"));
        assertTrue(sql.getValue().contains("EXPIRES_DATE <= CURRENT_TIMESTAMP"));
        assertTrue(sql.getValue().contains("FOR UPDATE"));
    }
}
