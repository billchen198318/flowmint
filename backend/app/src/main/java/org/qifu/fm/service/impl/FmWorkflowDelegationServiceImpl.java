package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmWorkflowDelegation;
import org.qifu.fm.mapper.FmWorkflowDelegationMapper;
import org.qifu.fm.service.IFmWorkflowDelegationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmWorkflowDelegationServiceImpl extends BaseService<FmWorkflowDelegation, String>
		implements IFmWorkflowDelegationService {

	private final FmWorkflowDelegationMapper mapper;

	public FmWorkflowDelegationServiceImpl(FmWorkflowDelegationMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmWorkflowDelegation, String> getBaseMapper() {
		return mapper;
	}
}
