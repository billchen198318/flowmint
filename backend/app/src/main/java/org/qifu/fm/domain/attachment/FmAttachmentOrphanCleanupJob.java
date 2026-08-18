package org.qifu.fm.domain.attachment;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.qifu.base.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FmAttachmentOrphanCleanupJob {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FmAttachmentOrphanCleanupJob.class);
    private static final int BATCH_SIZE = 100;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FmAttachmentStorageService storageService;

    public FmAttachmentOrphanCleanupJob(
            NamedParameterJdbcTemplate jdbcTemplate,
            FmAttachmentStorageService storageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
    }

    @Scheduled(initialDelay = 300000, fixedDelay = 21600000)
    public void quarantineOrphans() {
        try {
            for (FmAttachmentStorageService.FormalFile file
                    : storageService.findFormalFilesOlderThan(
                            Instant.now().minus(24, ChronoUnit.HOURS), BATCH_SIZE)) {
                if (hasDatabaseRecord(file)) continue;
                storageService.quarantine(file);
                LOGGER.warn("Quarantined FlowMint orphan attachment: tenant={}, fileOid={}",
                        file.tenantId(), file.fileOid());
            }
        } catch (ServiceException exception) {
            LOGGER.error("Unable to scan or quarantine FlowMint orphan attachments", exception);
        }
    }

    private boolean hasDatabaseRecord(FmAttachmentStorageService.FormalFile file) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM fm_attachment
                 WHERE TENANT_ID = :tenantId
                   AND FILE_OID = :fileOid
                """, new MapSqlParameterSource()
                        .addValue("tenantId", file.tenantId())
                        .addValue("fileOid", file.fileOid()), Integer.class);
        return count != null && count > 0;
    }
}
