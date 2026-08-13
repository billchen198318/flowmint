package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmAttachmentUploadFile;

@Mapper
public interface FmAttachmentUploadFileMapper
        extends IBaseMapper<FmAttachmentUploadFile, String> {
}
