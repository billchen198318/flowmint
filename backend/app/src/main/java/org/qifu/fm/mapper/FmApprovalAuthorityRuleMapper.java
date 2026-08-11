package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApprovalAuthorityRule;

@Mapper
public interface FmApprovalAuthorityRuleMapper extends IBaseMapper<FmApprovalAuthorityRule, String> {

    List<FmApprovalAuthorityRule> selectByAuthority(Map<String, Object> parameters);

    int deleteByAuthority(Map<String, Object> parameters);
}
