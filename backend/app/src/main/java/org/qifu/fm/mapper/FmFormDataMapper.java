package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmFormData;

@Mapper
public interface FmFormDataMapper extends IBaseMapper<FmFormData, String> {
}
