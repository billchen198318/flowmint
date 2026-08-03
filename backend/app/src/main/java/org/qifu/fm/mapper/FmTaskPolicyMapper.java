package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmTaskPolicy;

@Mapper
public interface FmTaskPolicyMapper extends IBaseMapper<FmTaskPolicy, String> {

    List<FmTaskPolicy> selectByVersion(Map<String, Object> paramMap);

    int deleteByVersion(Map<String, Object> paramMap);
}
