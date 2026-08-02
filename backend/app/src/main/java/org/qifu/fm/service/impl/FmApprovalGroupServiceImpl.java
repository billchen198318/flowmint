package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.mapper.FmApprovalGroupMapper;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmApprovalGroupServiceImpl extends BaseService<FmApprovalGroup, String>
		implements IFmApprovalGroupService {

	private final FmApprovalGroupMapper mapper;

	public FmApprovalGroupServiceImpl(FmApprovalGroupMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmApprovalGroup, String> getBaseMapper() {
		return mapper;
	}
}
