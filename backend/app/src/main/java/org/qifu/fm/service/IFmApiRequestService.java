package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmApiRequest;

public interface IFmApiRequestService extends IBaseService<FmApiRequest, String> {
	FmApiRequest findByIdempotency(String tenantId, String clientId, String hash);
	FmApiRequest findByExternalReference(String tenantId, String clientId,
			String sourceSystem, String sourceDocumentType, String sourceDocumentNo);
	int updateResult(FmApiRequest request);
}
