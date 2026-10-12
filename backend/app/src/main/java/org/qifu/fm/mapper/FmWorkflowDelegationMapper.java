package org.qifu.fm.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmWorkflowDelegation;

@Mapper
public interface FmWorkflowDelegationMapper extends IBaseMapper<FmWorkflowDelegation, String> {
	String lockTenantForWrite(@Param("tenantId") String tenantId);

	List<String> selectOverlappingActiveForUpdate(
			@Param("tenantId") String tenantId,
			@Param("principalAccount") String principalAccount,
			@Param("scopeType") String scopeType,
			@Param("scopeRefId") String scopeRefId,
			@Param("effectiveFrom") Date effectiveFrom,
			@Param("effectiveTo") Date effectiveTo,
			@Param("excludedOid") String excludedOid);
}
