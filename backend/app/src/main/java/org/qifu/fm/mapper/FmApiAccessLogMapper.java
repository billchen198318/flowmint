package org.qifu.fm.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApiAccessLog;

@Mapper
public interface FmApiAccessLogMapper extends IBaseMapper<FmApiAccessLog, String> {
	long countClientRequestsSince(@Param("tenantId") String tenantId,
			@Param("clientId") String clientId, @Param("since") Date since);
}
