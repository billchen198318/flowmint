package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmProcessStartPolicy;

@Mapper
public interface FmProcessStartPolicyMapper
        extends IBaseMapper<FmProcessStartPolicy, String> {

    List<FmProcessStartPolicy> selectByVersion(Map<String, Object> parameters);

    int deleteByVersion(Map<String, Object> parameters);
}
