package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApiClient;
import org.qifu.fm.mapper.FmApiClientMapper;
import org.qifu.fm.service.IFmApiClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmApiClientServiceImpl extends BaseService<FmApiClient, String>
		implements IFmApiClientService {
	private final FmApiClientMapper mapper;

	public FmApiClientServiceImpl(FmApiClientMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmApiClient, String> getBaseMapper() {
		return mapper;
	}
}
