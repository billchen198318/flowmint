package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmOrgLevelScheme;

@Mapper
public interface FmOrgLevelSchemeMapper
  extends IBaseMapper<FmOrgLevelScheme, String> {}
