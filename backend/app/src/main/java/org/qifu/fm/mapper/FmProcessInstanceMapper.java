package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessInstance;

@Mapper
public interface FmProcessInstanceMapper extends IBaseMapper<FmProcessInstance, String> {
}
