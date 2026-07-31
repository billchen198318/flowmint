package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.mapper.FmOrgApprovalLevelMapper;
import org.qifu.fm.service.IFmOrgApprovalLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgApprovalLevelServiceImpl extends BaseService<FmOrgApprovalLevel, String>
		implements IFmOrgApprovalLevelService {

	private final FmOrgApprovalLevelMapper mapper;

	public FmOrgApprovalLevelServiceImpl(FmOrgApprovalLevelMapper v) {
		mapper = v;
	}

	protected IBaseMapper<FmOrgApprovalLevel, String> getBaseMapper() {
		return mapper;
	}
}
