package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmDataActionVersion;
import org.qifu.fm.mapper.FmDataActionVersionMapper;
import org.qifu.fm.service.IFmDataActionVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(
		propagation = Propagation.REQUIRED,
		timeout = 300,
		readOnly = true)
public class FmDataActionVersionServiceImpl
		extends BaseService<FmDataActionVersion, String>
		implements IFmDataActionVersionService {

	private final FmDataActionVersionMapper mapper;

	public FmDataActionVersionServiceImpl(FmDataActionVersionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmDataActionVersion, String> getBaseMapper() {
		return mapper;
	}
}
