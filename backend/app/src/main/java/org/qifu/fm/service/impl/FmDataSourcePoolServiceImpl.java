package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.mapper.FmDataSourcePoolMapper;
import org.qifu.fm.service.IFmDataSourcePoolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmDataSourcePoolServiceImpl extends BaseService<FmDataSourcePool, String>
		implements IFmDataSourcePoolService {

	private final FmDataSourcePoolMapper mapper;

	public FmDataSourcePoolServiceImpl(FmDataSourcePoolMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmDataSourcePool, String> getBaseMapper() {
		return mapper;
	}
}
