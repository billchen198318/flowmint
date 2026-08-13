package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAttachmentUploadSession;
import org.qifu.fm.mapper.FmAttachmentUploadSessionMapper;
import org.qifu.fm.service.IFmAttachmentUploadSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAttachmentUploadSessionServiceImpl
        extends BaseService<FmAttachmentUploadSession, String>
        implements IFmAttachmentUploadSessionService {

    private final FmAttachmentUploadSessionMapper mapper;

    public FmAttachmentUploadSessionServiceImpl(FmAttachmentUploadSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmAttachmentUploadSession, String> getBaseMapper() {
        return mapper;
    }
}
