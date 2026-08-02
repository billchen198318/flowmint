package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApprovalGroupMember;
import org.qifu.fm.mapper.FmApprovalGroupMemberMapper;
import org.qifu.fm.service.IFmApprovalGroupMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmApprovalGroupMemberServiceImpl extends BaseService<FmApprovalGroupMember, String>
		implements IFmApprovalGroupMemberService {

	private final FmApprovalGroupMemberMapper mapper;

	public FmApprovalGroupMemberServiceImpl(FmApprovalGroupMemberMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmApprovalGroupMember, String> getBaseMapper() {
		return mapper;
	}
}
