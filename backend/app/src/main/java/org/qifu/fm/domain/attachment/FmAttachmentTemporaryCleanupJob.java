package org.qifu.fm.domain.attachment;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FmAttachmentTemporaryCleanupJob {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FmAttachmentTemporaryCleanupJob.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FmAttachmentStorageService storageService;

    public FmAttachmentTemporaryCleanupJob(
            NamedParameterJdbcTemplate jdbcTemplate,
            FmAttachmentStorageService storageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
    }

    @Scheduled(initialDelay = 120000, fixedDelay = 3600000)
    @Transactional
    public void cleanupExpiredSessions() {
        Date now = new Date();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("now", now).addValue("system", "SYSTEM");
        List<Map<String, Object>> sessions = jdbcTemplate.queryForList("""
                SELECT TENANT_ID, UPLOAD_SESSION_ID
                  FROM fm_attachment_upload_session
                 WHERE SESSION_STATUS = 'OPEN'
                   AND EXPIRES_DATE <= :now
                 ORDER BY EXPIRES_DATE
                 LIMIT 100
                """, parameters);
        for (Map<String, Object> session : sessions) {
            cleanupSession(String.valueOf(session.get("TENANT_ID")),
                    String.valueOf(session.get("UPLOAD_SESSION_ID")), now);
        }
    }

    private void cleanupSession(String tenantId, String sessionId, Date now) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId).addValue("sessionId", sessionId)
                .addValue("now", now).addValue("system", "SYSTEM");
        List<Map<String, Object>> files = jdbcTemplate.queryForList("""
                SELECT OID, FILE_OID
                  FROM fm_attachment_upload_file
                 WHERE TENANT_ID = :tenantId
                   AND UPLOAD_SESSION_ID = :sessionId
                   AND FILE_STATUS = 'TEMPORARY'
                 ORDER BY CDATE
                 LIMIT 100
                """, parameters);
        for (Map<String, Object> file : files) {
            try {
                storageService.deleteTemporary(
                        tenantId, sessionId, String.valueOf(file.get("FILE_OID")));
                jdbcTemplate.update("""
                        UPDATE fm_attachment_upload_file
                           SET FILE_STATUS = 'DELETED', UUSERID = :system, UDATE = :now
                         WHERE OID = :oid AND FILE_STATUS = 'TEMPORARY'
                        """, new MapSqlParameterSource(parameters.getValues())
                                .addValue("oid", file.get("OID")));
            } catch (ServiceException exception) {
                LOGGER.warn("Unable to clean expired FlowMint temporary attachment: tenant={}, session={}",
                        tenantId, sessionId, exception);
                return;
            }
        }
        Integer remaining = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM fm_attachment_upload_file
                 WHERE TENANT_ID = :tenantId AND UPLOAD_SESSION_ID = :sessionId
                   AND FILE_STATUS = 'TEMPORARY'
                """, parameters, Integer.class);
        if (remaining != null && remaining == 0) {
            jdbcTemplate.update("""
                    UPDATE fm_attachment_upload_session
                       SET SESSION_STATUS = 'EXPIRED', UUSERID = :system, UDATE = :now
                     WHERE TENANT_ID = :tenantId AND UPLOAD_SESSION_ID = :sessionId
                       AND SESSION_STATUS = 'OPEN'
                    """, parameters);
        }
    }
}
