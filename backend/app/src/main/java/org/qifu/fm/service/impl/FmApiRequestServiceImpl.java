package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApiRequest;
import org.qifu.fm.mapper.FmApiRequestMapper;
import org.qifu.fm.service.IFmApiRequestService;
import org.springframework.stereotype.Service;

@Service
public class FmApiRequestServiceImpl extends BaseService<FmApiRequest, String>
		implements IFmApiRequestService {
	private final FmApiRequestMapper mapper;

	public FmApiRequestServiceImpl(FmApiRequestMapper mapper) { this.mapper = mapper; }
	@Override protected IBaseMapper<FmApiRequest, String> getBaseMapper() { return mapper; }
	@Override public FmApiRequest findByIdempotency(String tenantId, String clientId,
			String hash) { return mapper.selectByIdempotency(tenantId, clientId, hash); }
	@Override public FmApiRequest findByExternalReference(String tenantId, String clientId,
			String sourceSystem, String sourceDocumentType, String sourceDocumentNo) {
		return mapper.selectByExternalReference(tenantId, clientId, sourceSystem,
				sourceDocumentType, sourceDocumentNo);
	}
	@Override public int updateResult(FmApiRequest request) { return mapper.updateResult(request); }
}
