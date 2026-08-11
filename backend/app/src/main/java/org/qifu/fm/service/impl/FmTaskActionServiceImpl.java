package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.mapper.FmTaskActionMapper;
import org.qifu.fm.service.IFmTaskActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskActionServiceImpl extends BaseService<FmTaskAction, String>
		implements IFmTaskActionService {

	private final FmTaskActionMapper mapper;

	public FmTaskActionServiceImpl(FmTaskActionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmTaskAction, String> getBaseMapper() {
		return mapper;
	}

}
