package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmDataAction;

@Mapper
public interface FmDataActionMapper extends IBaseMapper<FmDataAction, String> {

	int updateOptimistic(Map<String, Object> paramMap);
}
