package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApiClient;

@Mapper
public interface FmApiClientMapper extends IBaseMapper<FmApiClient, String> {
}
