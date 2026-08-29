package org.qifu.fm.domain.ai;

import org.qifu.base.exception.ServiceException;

public interface FmAiProviderClient {

	String providerType();

	FmAiAnalysisResponse analyze(FmAiProviderConfig config,
			FmAiAnalysisRequest request) throws ServiceException;
}
