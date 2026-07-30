package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmTenant;

@Mapper
public interface FmTenantMapper extends IBaseMapper<FmTenant, String> {
}