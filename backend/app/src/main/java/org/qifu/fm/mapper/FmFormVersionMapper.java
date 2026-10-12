package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmFormVersion;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FmFormVersionMapper extends IBaseMapper<FmFormVersion, String> {
    FmFormVersion selectForUpdate(@Param("oid") String oid);
}
