package org.qifu.fm.mapper;

import java.util.Date;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.entity.FmOrgUnitVersion;

@Mapper
public interface FmOrgUnitVersionMapper extends IBaseMapper<FmOrgUnitVersion, String> {

	List<FmOrgUnitView> selectCurrentTree(Map<String, Object> paramMap);

	List<FmOrgUnitView> findCurrentPage(Map<String, Object> paramMap);

	Long countCurrent(Map<String, Object> paramMap);

	FmOrgUnitView selectEffective(@Param("tenantId") String tenantId,
			@Param("orgUnitId") String orgUnitId,
			@Param("effectiveAt") Date effectiveAt);

	List<FmOrgUnitView> selectEffectiveTree(@Param("tenantId") String tenantId,
			@Param("effectiveAt") Date effectiveAt,
			@Param("includeInactive") boolean includeInactive);
}
