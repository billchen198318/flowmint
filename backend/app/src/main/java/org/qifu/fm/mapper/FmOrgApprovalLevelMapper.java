package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmOrgApprovalLevel;

@Mapper
public interface FmOrgApprovalLevelMapper
  extends IBaseMapper<FmOrgApprovalLevel, String> {}
