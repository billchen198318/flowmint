package org.qifu.fm.mapper;

import java.util.Date;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmApiClientKey;

@Mapper
public interface FmApiClientKeyMapper extends IBaseMapper<FmApiClientKey, String> {

	public FmApiClientKey selectByKeyId(@Param("keyId") String keyId);

	public int revoke(@Param("oid") String oid,
			@Param("tenantId") String tenantId,
			@Param("lockVersion") Integer lockVersion,
			@Param("revokedAt") Date revokedAt,
			@Param("revokedBy") String revokedBy,
			@Param("revokeReason") String revokeReason,
			@Param("udate") Date udate);

	public int markUsed(@Param("keyId") String keyId,
			@Param("usedAt") Date usedAt,
			@Param("sourceIp") String sourceIp);
}
