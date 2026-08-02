package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessDef;

@Mapper
public interface FmProcessDefMapper extends IBaseMapper<FmProcessDef, String> {
}