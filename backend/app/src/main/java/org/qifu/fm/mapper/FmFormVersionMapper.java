package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmFormVersion;

@Mapper
public interface FmFormVersionMapper extends IBaseMapper<FmFormVersion, String> {
}
