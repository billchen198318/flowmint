package org.qifu.fm.service;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmAiAnalysis;

public interface IFmAiAnalysisService extends IBaseService<FmAiAnalysis, String> {

	FmAiAnalysis findLatestSucceeded(String tenantId, String contentHash)
			throws ServiceException;
	int nextGenerationNo(String tenantId, String contentHash) throws ServiceException;
	void start(FmAiAnalysis analysis) throws ServiceException;
	void complete(FmAiAnalysis analysis) throws ServiceException;
	void fail(FmAiAnalysis analysis) throws ServiceException;
}
