package org.qifu.fm.domain.attachment;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class FmAttachmentBindingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FmAttachmentBindingService.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FmAttachmentStorageService storageService;

    public FmAttachmentBindingService(
            NamedParameterJdbcTemplate jdbcTemplate,
            FmAttachmentStorageService storageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
    }

    public void bind(String tenantId, String uploadSessionId, String account,
            String formId, Integer formVersionNo, String formDataId, Date now)
            throws ServiceException {
        if (StringUtils.isBlank(uploadSessionId)) return;
        MapSqlParameterSource sessionParameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("uploadSessionId", uploadSessionId)
                .addValue("account", account)
                .addValue("formId", formId)
                .addValue("formVersionNo", formVersionNo)
                .addValue("now", now);
        Integer sessionCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM fm_attachment_upload_session
                 WHERE TENANT_ID = :tenantId
                   AND UPLOAD_SESSION_ID = :uploadSessionId
                   AND OWNER_ACCOUNT = :account
                   AND FORM_ID = :formId
                   AND FORM_VERSION_NO = :formVersionNo
                   AND SESSION_STATUS = 'OPEN'
                   AND EXPIRES_DATE > :now
                """, sessionParameters, Integer.class);
        if (sessionCount == null || sessionCount != 1) {
            throw new ServiceException("附件上傳階段不存在、已逾期或不屬於目前使用者");
        }

        List<Map<String, Object>> files = jdbcTemplate.queryForList("""
                SELECT OID, ATTACHMENT_ID, FIELD_KEY, FILE_OID, FILE_NAME,
                       CONTENT_TYPE, FILE_SIZE, CONTENT_SHA256, SCAN_STATUS
                  FROM fm_attachment_upload_file
                 WHERE TENANT_ID = :tenantId
                   AND UPLOAD_SESSION_ID = :uploadSessionId
                   AND FILE_STATUS = 'TEMPORARY'
                 ORDER BY CDATE, OID
                """, sessionParameters);
        if (files.stream().anyMatch(file -> !"CLEAN".equals(file.get("SCAN_STATUS")))) {
            throw new ServiceException("附件尚未通過安全檢查，無法送出表單");
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("附件正式綁定必須在資料庫交易中執行");
        }

        for (Map<String, Object> file : files) {
            String fileOid = String.valueOf(file.get("FILE_OID"));
            Path formalPath = storageService.promote(tenantId, uploadSessionId, fileOid);
            registerRollbackCompensation(
                    tenantId, uploadSessionId, fileOid, formalPath);
            MapSqlParameterSource fileParameters = new MapSqlParameterSource()
                    .addValues(sessionParameters.getValues())
                    .addValue("oid", UUID.randomUUID().toString())
                    .addValue("attachmentId", file.get("ATTACHMENT_ID"))
                    .addValue("formDataId", formDataId)
                    .addValue("fieldKey", file.get("FIELD_KEY"))
                    .addValue("fileOid", fileOid)
                    .addValue("fileName", file.get("FILE_NAME"))
                    .addValue("contentType", file.get("CONTENT_TYPE"))
                    .addValue("fileSize", file.get("FILE_SIZE"))
                    .addValue("sha256", file.get("CONTENT_SHA256"));
            jdbcTemplate.update("""
                    INSERT INTO fm_attachment
                        (OID, TENANT_ID, ATTACHMENT_ID, FORM_DATA_ID, FIELD_KEY,
                         FILE_OID, FILE_NAME, CONTENT_TYPE, FILE_SIZE,
                         CONTENT_SHA256, STATUS, CUSERID, CDATE)
                    VALUES
                        (:oid, :tenantId, :attachmentId, :formDataId, :fieldKey,
                         :fileOid, :fileName, :contentType, :fileSize,
                         :sha256, 'ACTIVE', :account, :now)
                    """, fileParameters);
            jdbcTemplate.update("""
                    UPDATE fm_attachment_upload_file
                       SET FILE_STATUS = 'BOUND', UUSERID = :account, UDATE = :now
                     WHERE OID = :fileOidPk
                    """, new MapSqlParameterSource(fileParameters.getValues())
                            .addValue("fileOidPk", file.get("OID")));
        }
        jdbcTemplate.update("""
                UPDATE fm_attachment_upload_session
                   SET SESSION_STATUS = 'BOUND', UUSERID = :account, UDATE = :now
                 WHERE TENANT_ID = :tenantId
                   AND UPLOAD_SESSION_ID = :uploadSessionId
                """, sessionParameters);
    }

    void registerRollbackCompensation(
            String tenantId, String uploadSessionId, String fileOid, Path formalPath) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_ROLLED_BACK) return;
                        try {
                            storageService.restoreTemporary(
                                    tenantId, uploadSessionId, fileOid, formalPath);
                        } catch (ServiceException exception) {
                            LOGGER.error(
                                    "Unable to restore attachment after transaction rollback: "
                                            + "tenant={}, uploadBatch={}, fileOid={}",
                                    tenantId, uploadSessionId, fileOid, exception);
                        }
                    }
                });
    }
}
