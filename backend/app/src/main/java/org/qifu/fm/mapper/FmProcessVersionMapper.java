package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessVersion;

@Mapper
public interface FmProcessVersionMapper extends IBaseMapper<FmProcessVersion, String> {
}