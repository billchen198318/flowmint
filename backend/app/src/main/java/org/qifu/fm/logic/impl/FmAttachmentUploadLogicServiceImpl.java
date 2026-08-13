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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FmAttachmentUploadLogicServiceImpl
        implements IFmAttachmentUploadLogicService {

    private static final long SESSION_LIFETIME_MILLIS = 24L * 60L * 60L * 1000L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    private final IFmAttachmentUploadSessionService sessionService;
    private final IFmAttachmentUploadFileService fileService;
    private final IFmFormVersionService formVersionService;
    private final IFmTenantAccountService tenantAccountService;
    private final FmAttachmentStorageService storageService;

    public FmAttachmentUploadLogicServiceImpl(
            IFmAttachmentUploadSessionService sessionService,
            IFmAttachmentUploadFileService fileService,
            IFmFormVersionService formVersionService,
            IFmTenantAccountService tenantAccountService,
            FmAttachmentStorageService storageService) {
        this.sessionService = sessionService;
        this.fileService = fileService;
        this.formVersionService = formVersionService;
        this.tenantAccountService = tenantAccountService;
        this.storageService = storageService;
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
        requireSession(tenantId, sessionId, account);
        validateUpload(fieldKey, file);
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
            if (!storageService.hasExpectedSignature(stored.path(), value.getContentType())) {
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
                .findFirst().orElseThrow(() -> new ServiceException("Upload Session 不存在或已過期"));
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

    private void validateUpload(String fieldKey, MultipartFile file) throws ServiceException {
        if (StringUtils.isBlank(fieldKey) || fieldKey.length() > 100) {
            throw new ServiceException("附件欄位不合法");
        }
        if (file == null || file.isEmpty()) throw new ServiceException("附件不可為空");
        String name = safeFileName(file.getOriginalFilename());
        String extension = StringUtils.substringAfterLast(name, ".").toLowerCase(Locale.ROOT);
        String contentType = StringUtils.defaultString(file.getContentType())
                .toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)
                || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ServiceException("只允許 PDF、JPG、JPEG、PNG 附件");
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
}
