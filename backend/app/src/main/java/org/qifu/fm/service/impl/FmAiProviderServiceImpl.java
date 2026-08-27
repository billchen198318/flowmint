package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmAiProvider;
import org.qifu.fm.mapper.FmAiProviderMapper;
import org.qifu.fm.service.IFmAiProviderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAiProviderServiceImpl extends BaseService<FmAiProvider, String>
		implements IFmAiProviderService {

	private final FmAiProviderMapper mapper;

	public FmAiProviderServiceImpl(FmAiProviderMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmAiProvider, String> getBaseMapper() {
		return mapper;
	}
}
