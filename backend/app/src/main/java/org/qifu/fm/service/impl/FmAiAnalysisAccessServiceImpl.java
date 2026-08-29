package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAiAnalysisAccess;
import org.qifu.fm.mapper.FmAiAnalysisAccessMapper;
import org.qifu.fm.service.IFmAiAnalysisAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAiAnalysisAccessServiceImpl
		extends BaseService<FmAiAnalysisAccess, String>
		implements IFmAiAnalysisAccessService {

	private final FmAiAnalysisAccessMapper mapper;

	public FmAiAnalysisAccessServiceImpl(FmAiAnalysisAccessMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmAiAnalysisAccess, String> getBaseMapper() {
		return mapper;
	}
}
