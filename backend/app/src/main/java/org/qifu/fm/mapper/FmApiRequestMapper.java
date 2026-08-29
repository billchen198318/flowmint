package org.qifu.fm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApiRequest;

@Mapper
public interface FmApiRequestMapper extends IBaseMapper<FmApiRequest, String> {
	FmApiRequest selectByIdempotency(@Param("tenantId") String tenantId,
			@Param("clientId") String clientId,
			@Param("idempotencyKeyHash") String idempotencyKeyHash);
	FmApiRequest selectByExternalReference(@Param("tenantId") String tenantId,
			@Param("clientId") String clientId,
			@Param("sourceSystem") String sourceSystem,
			@Param("sourceDocumentType") String sourceDocumentType,
			@Param("sourceDocumentNo") String sourceDocumentNo);
	int updateResult(FmApiRequest request);
	@Override List<FmApiRequest> selectListByParams(Map<String, Object> parameters);
}
