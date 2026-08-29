package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.UUID;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApiAccessLog;
import org.qifu.fm.mapper.FmApiAccessLogMapper;
import org.qifu.fm.service.IFmApiAccessLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmApiAccessLogServiceImpl extends BaseService<FmApiAccessLog, String>
		implements IFmApiAccessLogService {
	private final FmApiAccessLogMapper mapper;
	public FmApiAccessLogServiceImpl(FmApiAccessLogMapper mapper) { this.mapper = mapper; }
	@Override
	protected IBaseMapper<FmApiAccessLog, String> getBaseMapper() { return mapper; }
	@Override
	public long countClientRequestsSince(String tenantId, String clientId, Date since) {
		return mapper.countClientRequestsSince(tenantId, clientId, since);
	}
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void append(FmApiAccessLog accessLog) {
		accessLog.setOid(UUID.randomUUID().toString());
		accessLog.setCdate(new Date());
		mapper.insert(accessLog);
	}
}
