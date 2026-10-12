package org.qifu.fm.domain.attachment;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FmAttachmentBindingService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FmAttachmentBindingService.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FmAttachmentStorageService storageService;
    private final ObjectMapper objectMapper;

    public FmAttachmentBindingService(
            NamedParameterJdbcTemplate jdbcTemplate,
            FmAttachmentStorageService storageService,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    public void bind(String tenantId, String uploadSessionId, String account,
            String formId, Integer formVersionNo, String formDataId, Date now,
            String schemaContent, Map<String, Object> formData)
            throws ServiceException {
        Set<String> submittedFiles = submittedFiles(schemaContent, formData);
        if (StringUtils.isBlank(uploadSessionId)) {
            if (!submittedFiles.isEmpty()) {
                throw new ServiceException("表單包含附件，但缺少附件上傳階段");
            }
            return;
        }
        MapSqlParameterSource sessionParameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("uploadSessionId", uploadSessionId)
                .addValue("account", account)
                .addValue("formId", formId)
                .addValue("formVersionNo", formVersionNo)
                .addValue("now", now);
        List<String> sessions = jdbcTemplate.queryForList("""
                SELECT OID
                  FROM fm_attachment_upload_session
                 WHERE TENANT_ID = :tenantId
                   AND UPLOAD_SESSION_ID = :uploadSessionId
                   AND OWNER_ACCOUNT = :account
                   AND FORM_ID = :formId
                   AND FORM_VERSION_NO = :formVersionNo
                   AND SESSION_STATUS = 'OPEN'
                   AND EXPIRES_DATE > CURRENT_TIMESTAMP
                 FOR UPDATE
                """, sessionParameters, String.class);
        if (sessions.size() != 1) {
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
        assertMatchingFiles(submittedFiles, files);
        if (files.stream().anyMatch(file -> !"CLEAN".equals(file.get("SCAN_STATUS")))) {
            throw new ServiceException("附件尚未通過安全檢查，無法送出表單");
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("附件正式綁定必須在資料庫交易中執行");
        }

        for (Map<String, Object> file : files) {
            String fileOid = String.valueOf(file.get("FILE_OID"));
            storageService.verifyTemporary(tenantId, uploadSessionId, fileOid,
                    ((Number) file.get("FILE_SIZE")).longValue(),
                    String.valueOf(file.get("CONTENT_SHA256")));
            Path formalPath = storageService.promote(
                    tenantId, uploadSessionId, fileOid, now);
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

    Set<String> submittedFiles(String schemaContent, Map<String, Object> data)
            throws ServiceException {
        try {
            Set<String> files = new HashSet<>();
            collectSubmittedFiles(objectMapper.readTree(schemaContent).path("components"),
                    data, files);
            return files;
        } catch (RuntimeException exception) {
            throw new ServiceException("附件欄位資料無法驗證");
        }
    }

    private void collectSubmittedFiles(JsonNode components, Map<String, Object> data,
            Set<String> files) throws ServiceException {
        for (JsonNode component : components) {
            String key = component.path("key").asString("");
            String type = component.path("type").asString("");
            Object value = data == null ? null : data.get(key);
            if ("file".equals(type)) {
                if (value == null || "".equals(value)) continue;
                if (value instanceof String id) {
                    addSubmittedFile(files, key, id);
                } else if (value instanceof Iterable<?> ids) {
                    for (Object id : ids) {
                        if (!(id instanceof String text)) {
                            throw new ServiceException("附件欄位 ID 格式不正確：" + key);
                        }
                        addSubmittedFile(files, key, text);
                    }
                } else {
                    throw new ServiceException("附件欄位 ID 格式不正確：" + key);
                }
            } else if ("container".equals(type) && value instanceof Map<?, ?> nested) {
                collectSubmittedFiles(component.path("components"), stringMap(nested), files);
            } else if (("datagrid".equals(type) || "editgrid".equals(type))
                    && value instanceof Iterable<?> rows) {
                for (Object row : rows) {
                    if (row instanceof Map<?, ?> nested) {
                        collectSubmittedFiles(component.path("components"),
                                stringMap(nested), files);
                    }
                }
            } else {
                collectSubmittedFiles(component.path("components"), data, files);
                for (JsonNode column : component.path("columns")) {
                    collectSubmittedFiles(column.path("components"), data, files);
                }
                for (JsonNode row : component.path("rows")) {
                    for (JsonNode cell : row) {
                        collectSubmittedFiles(cell.path("components"), data, files);
                    }
                }
            }
        }
    }

    private Map<String, Object> stringMap(Map<?, ?> source) {
        Map<String, Object> result = new java.util.HashMap<>();
        source.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private void addSubmittedFile(Set<String> files, String fieldKey, String id)
            throws ServiceException {
        if (StringUtils.isBlank(id) || !files.add(fieldKey + "\u0000" + id)) {
            throw new ServiceException("附件欄位包含空白或重複的附件 ID：" + fieldKey);
        }
    }

    void assertMatchingFiles(Set<String> submittedFiles, List<Map<String, Object>> files)
            throws ServiceException {
        Set<String> uploadedFiles = new HashSet<>();
        for (Map<String, Object> file : files) {
            String fieldKey = String.valueOf(file.get("FIELD_KEY"));
            String id = String.valueOf(file.get("ATTACHMENT_ID"));
            if (!uploadedFiles.add(fieldKey + "\u0000" + id)) {
                throw new ServiceException("附件上傳階段包含重複附件 ID");
            }
        }
        if (!uploadedFiles.equals(submittedFiles)) {
            throw new ServiceException("表單附件欄位與本次上傳階段的檔案不一致");
        }
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
