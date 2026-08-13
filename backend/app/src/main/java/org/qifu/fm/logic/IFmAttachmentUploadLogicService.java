package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.view.FmAttachmentSessionView;
import org.qifu.fm.dto.view.FmAttachmentUploadView;
import org.springframework.web.multipart.MultipartFile;

public interface IFmAttachmentUploadLogicService {

    DefaultResult<FmAttachmentSessionView> createSession(
            String tenantId, String formId, Integer formVersionNo) throws ServiceException;

    DefaultResult<FmAttachmentUploadView> upload(
            String tenantId, String sessionId, String fieldKey, MultipartFile file)
            throws ServiceException;

    DefaultResult<List<FmAttachmentUploadView>> list(
            String tenantId, String sessionId) throws ServiceException;

    DefaultResult<Boolean> delete(
            String tenantId, String sessionId, String attachmentId) throws ServiceException;
}
