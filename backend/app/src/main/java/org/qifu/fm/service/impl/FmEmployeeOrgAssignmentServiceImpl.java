package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmEmployeeOrgAssignment;
import org.qifu.fm.mapper.FmEmployeeOrgAssignmentMapper;
import org.qifu.fm.service.IFmEmployeeOrgAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmEmployeeOrgAssignmentServiceImpl extends BaseService<FmEmployeeOrgAssignment, String>
		implements IFmEmployeeOrgAssignmentService {

	private final FmEmployeeOrgAssignmentMapper mapper;

	public FmEmployeeOrgAssignmentServiceImpl(FmEmployeeOrgAssignmentMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmEmployeeOrgAssignment, String> getBaseMapper() {
		return mapper;
	}
}
