package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAttachmentUploadFile;
import org.qifu.fm.mapper.FmAttachmentUploadFileMapper;
import org.qifu.fm.service.IFmAttachmentUploadFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAttachmentUploadFileServiceImpl
        extends BaseService<FmAttachmentUploadFile, String>
        implements IFmAttachmentUploadFileService {

    private final FmAttachmentUploadFileMapper mapper;

    public FmAttachmentUploadFileServiceImpl(FmAttachmentUploadFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmAttachmentUploadFile, String> getBaseMapper() {
        return mapper;
    }
}
