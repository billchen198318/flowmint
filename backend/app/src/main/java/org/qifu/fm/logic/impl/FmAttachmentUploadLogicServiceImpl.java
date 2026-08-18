package org.qifu.fm.logic.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.attachment.FmAttachmentStorageService;
import org.qifu.fm.dto.view.FmAttachmentSessionView;
import org.qifu.fm.dto.view.FmAttachmentUploadView;
import org.qifu.fm.entity.FmAttachmentUploadFile;
import org.qifu.fm.entity.FmAttachmentUploadSession;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmAttachmentUploadLogicService;
import org.qifu.fm.service.IFmAttachmentUploadFileService;
import org.qifu.fm.service.IFmAttachmentUploadSessionService;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class FmAttachmentUploadLogicServiceImpl
        implements IFmAttachmentUploadLogicService {

    private static final long SESSION_LIFETIME_MILLIS = 24L * 60L * 60L * 1000L;
    private static final int ACCOUNT_UPLOADS_PER_MINUTE = 30;
    private static final int TENANT_UPLOADS_PER_MINUTE = 200;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "bmp",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "zip", "7z", "rar");

    private final IFmAttachmentUploadSessionService sessionService;
    private final IFmAttachmentUploadFileService fileService;
    private final IFmFormVersionService formVersionService;
    private final IFmTenantAccountService tenantAccountService;
    private final FmAttachmentStorageService storageService;
    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public FmAttachmentUploadLogicServiceImpl(
            IFmAttachmentUploadSessionService sessionService,
            IFmAttachmentUploadFileService fileService,
            IFmFormVersionService formVersionService,
            IFmTenantAccountService tenantAccountService,
            FmAttachmentStorageService storageService,
            ObjectMapper objectMapper,
            NamedParameterJdbcTemplate jdbcTemplate) {
        this.sessionService = sessionService;
        this.fileService = fileService;
        this.formVersionService = formVersionService;
        this.tenantAccountService = tenantAccountService;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefaultResult<FmAttachmentSessionView> createSession(
            String tenantId, String formId, Integer formVersionNo) throws ServiceException {
        String account = currentAccount(tenantId);
        validateFormVersion(tenantId, formId, formVersionNo);
        Date now = new Date();
        FmAttachmentUploadSession session = new FmAttachmentUploadSession();
        session.setOid(UUID.randomUUID().toString());
        session.setTenantId(tenantId);
        session.setUploadSessionId(UUID.randomUUID().toString());
        session.setOwnerAccount(account);
        session.setFormId(formId);
        session.setFormVersionNo(formVersionNo);
        session.setSessionStatus("OPEN");
        session.setExpiresDate(new Date(now.getTime() + SESSION_LIFETIME_MILLIS));
        session.setCuserid(account);
        session.setCdate(now);
        sessionService.insert(session).getValueEmptyThrowMessage();
        return success(new FmAttachmentSessionView(
                session.getUploadSessionId(), session.getExpiresDate()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefaultResult<FmAttachmentUploadView> upload(
            String tenantId, String sessionId, String fieldKey, MultipartFile file)
            throws ServiceException {
        String account = currentAccount(tenantId);
        FmAttachmentUploadSession session = requireSession(tenantId, sessionId, account);
        validateUploadRate(tenantId, account);
        AttachmentRule rule = attachmentRule(session, fieldKey);
        validateUpload(fieldKey, file, rule);
        validateFileCount(tenantId, sessionId, fieldKey, rule.maxFiles());
        validateTotalSize(tenantId, sessionId, fieldKey, file.getSize(), rule.maxTotalSize());
        FmAttachmentStorageService.StoredFile stored;
        try {
            stored = storageService.storeTemporary(
                    tenantId, sessionId, file.getInputStream(), file.getSize());
        } catch (IOException exception) {
            throw new ServiceException("無法讀取上傳附件");
        }
        try {
            Date now = new Date();
            FmAttachmentUploadFile value = new FmAttachmentUploadFile();
            value.setOid(UUID.randomUUID().toString());
            value.setTenantId(tenantId);
            value.setAttachmentId(UUID.randomUUID().toString());
            value.setUploadSessionId(sessionId);
            value.setFieldKey(fieldKey.trim());
            value.setFileOid(stored.fileOid());
            value.setFileName(safeFileName(file.getOriginalFilename()));
            value.setContentType(file.getContentType().toLowerCase(Locale.ROOT));
            value.setFileSize(stored.size());
            value.setContentSha256(stored.sha256());
            value.setFileStatus("TEMPORARY");
            String extension = StringUtils.substringAfterLast(
                    value.getFileName(), ".").toLowerCase(Locale.ROOT);
            if (!storageService.hasExpectedSignature(stored.path(), extension)) {
                storageService.deleteTemporary(tenantId, sessionId, stored.fileOid());
                throw new ServiceException("附件內容與檔案類型不符");
            }
            value.setScanStatus("CLEAN");
            value.setCuserid(account);
            value.setCdate(now);
            fileService.insert(value).getValueEmptyThrowMessage();
            return success(view(value));
        } catch (ServiceException exception) {
            storageService.deleteTemporary(tenantId, sessionId, stored.fileOid());
            throw exception;
        }
    }

    @Override
    public DefaultResult<List<FmAttachmentUploadView>> list(
            String tenantId, String sessionId) throws ServiceException {
        String account = currentAccount(tenantId);
        requireSession(tenantId, sessionId, account);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("uploadSessionId", sessionId);
        parameters.put("fileStatus", "TEMPORARY");
        return success(fileService.selectListByParams(parameters, "CDATE", "ASC")
                .getValue().stream().map(this::view).toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DefaultResult<Boolean> delete(
            String tenantId, String sessionId, String attachmentId) throws ServiceException {
        String account = currentAccount(tenantId);
        requireSession(tenantId, sessionId, account);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("uploadSessionId", sessionId);
        parameters.put("attachmentId", attachmentId);
        parameters.put("fileStatus", "TEMPORARY");
        FmAttachmentUploadFile file = fileService.selectListByParams(parameters).getValue()
                .stream().findFirst().orElseThrow(() -> new ServiceException("找不到暫存附件"));
        storageService.deleteTemporary(tenantId, sessionId, file.getFileOid());
        file.setFileStatus("DELETED");
        file.setUuserid(account);
        file.setUdate(new Date());
        fileService.update(file).getValueEmptyThrowMessage();
        return success(Boolean.TRUE);
    }

    private FmAttachmentUploadSession requireSession(
            String tenantId, String sessionId, String account) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("uploadSessionId", sessionId);
        parameters.put("ownerAccount", account);
        parameters.put("sessionStatus", "OPEN");
        Date now = new Date();
        return sessionService.selectListByParams(parameters).getValue().stream()
                .filter(value -> value.getExpiresDate() != null && value.getExpiresDate().after(now))
                .findFirst().orElseThrow(() -> new ServiceException("附件上傳批次不存在或已過期"));
    }

    private void validateFormVersion(String tenantId, String formId, Integer versionNo)
            throws ServiceException {
        if (StringUtils.isBlank(formId) || versionNo == null) {
            throw new ServiceException("表單及版本不可為空");
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        parameters.put("versionNo", versionNo);
        FmFormVersion version = formVersionService.selectListByParams(parameters).getValue()
                .stream().findFirst().orElse(null);
        if (version == null || !Set.of("DRAFT", "PUBLISHED").contains(version.getVersionStatus())) {
            throw new ServiceException("找不到可使用的表單版本");
        }
    }

    private void validateUpload(
            String fieldKey, MultipartFile file, AttachmentRule rule) throws ServiceException {
        if (StringUtils.isBlank(fieldKey) || fieldKey.length() > 100) {
            throw new ServiceException("附件欄位不合法");
        }
        if (file == null || file.isEmpty()) throw new ServiceException("附件不可為空");
        String name = safeFileName(file.getOriginalFilename());
        String extension = StringUtils.substringAfterLast(name, ".").toLowerCase(Locale.ROOT);
        String contentType = StringUtils.defaultString(file.getContentType())
                .toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)
                || !rule.extensions().contains(extension)
                || !extensionMatchesContentType(extension, contentType)) {
            throw new ServiceException("附件格式或 Content-Type 不在允許範圍內");
        }
        if (file.getSize() > rule.maxFileSize()) {
            throw new ServiceException("附件大小超過此欄位設定上限");
        }
    }

    private boolean extensionMatchesContentType(String extension, String contentType) {
        return switch (extension) {
            case "pdf" -> "application/pdf".equals(contentType);
            case "jpg", "jpeg" -> "image/jpeg".equals(contentType);
            case "png" -> "image/png".equals(contentType);
            case "bmp" -> Set.of("image/bmp", "image/x-ms-bmp").contains(contentType);
            case "doc" -> "application/msword".equals(contentType);
            case "xls" -> "application/vnd.ms-excel".equals(contentType);
            case "ppt" -> "application/vnd.ms-powerpoint".equals(contentType);
            case "docx" -> ("application/vnd.openxmlformats-officedocument"
                    + ".wordprocessingml.document").equals(contentType);
            case "xlsx" -> ("application/vnd.openxmlformats-officedocument"
                    + ".spreadsheetml.sheet").equals(contentType);
            case "pptx" -> ("application/vnd.openxmlformats-officedocument"
                    + ".presentationml.presentation").equals(contentType);
            case "zip" -> Set.of("application/zip", "application/x-zip-compressed")
                    .contains(contentType);
            case "7z" -> Set.of("application/x-7z-compressed", "application/octet-stream")
                    .contains(contentType);
            case "rar" -> Set.of("application/vnd.rar", "application/x-rar-compressed",
                    "application/octet-stream").contains(contentType);
            default -> false;
        };
    }

    private AttachmentRule attachmentRule(
            FmAttachmentUploadSession session, String fieldKey) throws ServiceException {
        FmFormVersion version = findFormVersion(
                session.getTenantId(), session.getFormId(), session.getFormVersionNo());
        try {
            JsonNode component = findComponent(
                    objectMapper.readTree(version.getSchemaContent()).path("components"), fieldKey);
            if (component == null || !"file".equals(component.path("type").asText())) {
                throw new ServiceException("指定欄位不是此表單版本的附件元件");
            }
            Set<String> extensions = configuredExtensions(component.path("fileTypes"));
            long maximum = configuredMaximumSize(component.path("fileMaxSize").asText(""));
            int maxFiles = component.path("maxNumberOfFiles").asInt(
                    component.path("multiple").asBoolean(false) ? 10 : 1);
            if (maxFiles < 1 || maxFiles > 20) {
                throw new ServiceException("附件數量設定必須介於 1 至 20");
            }
            long maximumTotal = configuredMaximumSize(
                    component.path("flowmintMaxTotalSize").asText(
                            Math.min(maximum * maxFiles, 50L * 1024L * 1024L) + ""));
            if (maximumTotal < maximum || maximumTotal > 50L * 1024L * 1024L) {
                throw new ServiceException("附件總容量上限不得小於單檔上限且不可超過 50MB");
            }
            return new AttachmentRule(extensions, maximum, maxFiles, maximumTotal);
        } catch (ServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ServiceException("無法讀取附件欄位設定");
        }
    }

    private JsonNode findComponent(JsonNode components, String fieldKey) {
        for (JsonNode component : components) {
            if (fieldKey.equals(component.path("key").asText())) return component;
            JsonNode found = findComponent(component.path("components"), fieldKey);
            if (found != null) return found;
            for (JsonNode column : component.path("columns")) {
                found = findComponent(column.path("components"), fieldKey);
                if (found != null) return found;
            }
            for (JsonNode row : component.path("rows")) {
                for (JsonNode cell : row) {
                    found = findComponent(cell.path("components"), fieldKey);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    private Set<String> configuredExtensions(JsonNode fileTypes) {
        Set<String> configured = new java.util.HashSet<>();
        for (JsonNode fileType : fileTypes) {
            String value = fileType.isTextual()
                    ? fileType.asText() : fileType.path("value").asText("");
            for (String token : value.toLowerCase(Locale.ROOT).split("[, ]+")) {
                String extension = StringUtils.removeStart(token.trim(), ".");
                if (ALLOWED_EXTENSIONS.contains(extension)) configured.add(extension);
            }
        }
        return configured.isEmpty() ? ALLOWED_EXTENSIONS : Set.copyOf(configured);
    }

    private long configuredMaximumSize(String value) throws ServiceException {
        if (StringUtils.isBlank(value)) return 8L * 1024L * 1024L;
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace(" ", "");
        try {
            if (normalized.endsWith("MB")) {
                return Long.parseLong(StringUtils.removeEnd(normalized, "MB")) * 1024L * 1024L;
            }
            if (normalized.endsWith("KB")) {
                return Long.parseLong(StringUtils.removeEnd(normalized, "KB")) * 1024L;
            }
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            throw new ServiceException("附件大小設定格式錯誤，請使用 KB 或 MB");
        }
    }

    private void validateFileCount(
            String tenantId, String sessionId, String fieldKey, int maximum)
            throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("uploadSessionId", sessionId);
        parameters.put("fieldKey", fieldKey);
        parameters.put("fileStatus", "TEMPORARY");
        if (fileService.count(parameters) >= maximum) {
            throw new ServiceException("附件數量已達此欄位設定上限");
        }
    }

    private void validateTotalSize(
            String tenantId, String sessionId, String fieldKey,
            long incomingSize, long maximum) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("uploadSessionId", sessionId);
        parameters.put("fieldKey", fieldKey);
        parameters.put("fileStatus", "TEMPORARY");
        long currentSize = fileService.selectListByParams(parameters).getValue().stream()
                .map(FmAttachmentUploadFile::getFileSize)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        if (currentSize + incomingSize > maximum) {
            throw new ServiceException("附件總容量超過此欄位設定上限");
        }
    }

    private void validateUploadRate(String tenantId, String account) throws ServiceException {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("account", account);
        Map<String, Object> counts = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS TENANT_COUNT,
                       SUM(CASE WHEN s.OWNER_ACCOUNT = :account THEN 1 ELSE 0 END)
                           AS ACCOUNT_COUNT
                  FROM fm_attachment_upload_file f
                  JOIN fm_attachment_upload_session s
                    ON s.TENANT_ID = f.TENANT_ID
                   AND s.UPLOAD_SESSION_ID = f.UPLOAD_SESSION_ID
                 WHERE f.TENANT_ID = :tenantId
                   AND f.CDATE >= DATE_SUB(NOW(3), INTERVAL 1 MINUTE)
                """, parameters);
        long tenantCount = ((Number) counts.get("TENANT_COUNT")).longValue();
        Number accountValue = (Number) counts.get("ACCOUNT_COUNT");
        long accountCount = accountValue == null ? 0 : accountValue.longValue();
        if (accountCount >= ACCOUNT_UPLOADS_PER_MINUTE) {
            throw new ServiceException("附件上傳過於頻繁，請稍後再試");
        }
        if (tenantCount >= TENANT_UPLOADS_PER_MINUTE) {
            throw new ServiceException("Tenant 附件上傳量已達每分鐘上限，請稍後再試");
        }
    }

    private String safeFileName(String original) throws ServiceException {
        String name = StringUtils.trimToEmpty(original);
        if (StringUtils.isBlank(name) || name.length() > 255
                || name.contains("/") || name.contains("\\") || name.contains("..")
                || name.chars().anyMatch(value -> value < 32 || value == 127)) {
            throw new ServiceException("附件檔名不合法");
        }
        return name;
    }

    private String currentAccount(String tenantId) throws ServiceException {
        if (StringUtils.isBlank(tenantId)) throw new ServiceException("Tenant 不可為空");
        String account = UserUtils.getCurrentUser().getUsername();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("account", account);
        parameters.put("status", "ACTIVE");
        Date now = new Date();
        boolean active = tenantAccountService.selectListByParams(parameters).getValue().stream()
                .anyMatch(value -> effective(value, now));
        if (!active) throw new ServiceException("目前帳號不屬於指定 Tenant");
        return account;
    }

    private boolean effective(FmTenantAccount value, Date now) {
        return (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
                && (value.getEffectiveTo() == null || value.getEffectiveTo().after(now));
    }

    private FmAttachmentUploadView view(FmAttachmentUploadFile value) {
        return new FmAttachmentUploadView(
                value.getAttachmentId(), value.getFieldKey(), value.getFileName(),
                value.getContentType(), value.getFileSize(), value.getScanStatus());
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }

    private FmFormVersion findFormVersion(
            String tenantId, String formId, Integer versionNo) throws ServiceException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("formId", formId);
        parameters.put("versionNo", versionNo);
        return formVersionService.selectListByParams(parameters).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("找不到表單版本"));
    }

    private record AttachmentRule(
            Set<String> extensions,
            long maxFileSize,
            int maxFiles,
            long maxTotalSize) {
    }
}
