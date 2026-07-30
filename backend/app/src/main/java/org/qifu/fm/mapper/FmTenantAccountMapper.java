package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmTenantAccount;

@Mapper
public interface FmTenantAccountMapper extends IBaseMapper<FmTenantAccount, String> {
}