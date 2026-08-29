package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAiAnalysis;
import org.qifu.fm.mapper.FmAiAnalysisMapper;
import org.qifu.fm.service.IFmAiAnalysisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAiAnalysisServiceImpl extends BaseService<FmAiAnalysis, String>
		implements IFmAiAnalysisService {

	private final FmAiAnalysisMapper mapper;

	public FmAiAnalysisServiceImpl(FmAiAnalysisMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmAiAnalysis, String> getBaseMapper() {
		return mapper;
	}

	@Override
	public FmAiAnalysis findLatestSucceeded(String tenantId, String contentHash) {
		return mapper.findLatestSucceeded(parameters(tenantId, contentHash));
	}

	@Override
	public int nextGenerationNo(String tenantId, String contentHash) {
		Integer value = mapper.findNextGenerationNo(parameters(tenantId, contentHash));
		return value == null ? 1 : value;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void start(FmAiAnalysis analysis) throws ServiceException {
		insert(analysis);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void complete(FmAiAnalysis analysis) throws ServiceException {
		if (mapper.complete(statusParameters(analysis)) != 1) {
			throw new ServiceException("AI Analysis 狀態已變更，無法重複完成");
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void fail(FmAiAnalysis analysis) throws ServiceException {
		if (mapper.fail(statusParameters(analysis)) != 1) {
			throw new ServiceException("AI Analysis 狀態已變更，無法重複標記失敗");
		}
	}

	private Map<String, Object> parameters(String tenantId, String contentHash) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("contentHash", contentHash);
		return parameters;
	}

	private Map<String, Object> statusParameters(FmAiAnalysis analysis) {
		Map<String, Object> parameters = parameters(
				analysis.getTenantId(), analysis.getContentHash());
		parameters.put("analysisId", analysis.getAnalysisId());
		parameters.put("completeDate", analysis.getCompleteDate());
		parameters.put("elapsedMillis", analysis.getElapsedMillis());
		parameters.put("inputTokens", analysis.getInputTokens());
		parameters.put("outputTokens", analysis.getOutputTokens());
		parameters.put("resultContent", analysis.getResultContent());
		parameters.put("errorCode", analysis.getErrorCode());
		return parameters;
	}
}
