package org.qifu.fm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmOrgUnit;

@Mapper
public interface FmOrgUnitMapper extends IBaseMapper<FmOrgUnit, String> {
	int advanceVersion(@Param("oid") String oid, @Param("expectedVersionNo") int expectedVersionNo);
}
