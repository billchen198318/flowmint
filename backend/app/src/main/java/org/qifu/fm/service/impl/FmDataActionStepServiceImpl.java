package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.mapper.FmDataActionStepMapper;
import org.qifu.fm.service.IFmDataActionStepService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(
		propagation = Propagation.REQUIRED,
		timeout = 300,
		readOnly = true)
public class FmDataActionStepServiceImpl
		extends BaseService<FmDataActionStep, String>
		implements IFmDataActionStepService {

	private final FmDataActionStepMapper mapper;

	public FmDataActionStepServiceImpl(FmDataActionStepMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmDataActionStep, String> getBaseMapper() {
		return mapper;
	}
}
