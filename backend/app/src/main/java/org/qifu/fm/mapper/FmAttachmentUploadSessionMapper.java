package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmAttachmentUploadSession;

@Mapper
public interface FmAttachmentUploadSessionMapper
        extends IBaseMapper<FmAttachmentUploadSession, String> {
}
