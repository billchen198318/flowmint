package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskAssignmentSnapshotDtl;
import org.qifu.fm.mapper.FmTaskAssignmentSnapshotDtlMapper;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotDtlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskAssignmentSnapshotDtlServiceImpl
		extends BaseService<FmTaskAssignmentSnapshotDtl, String>
		implements IFmTaskAssignmentSnapshotDtlService {

	private final FmTaskAssignmentSnapshotDtlMapper mapper;

	public FmTaskAssignmentSnapshotDtlServiceImpl(
			FmTaskAssignmentSnapshotDtlMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmTaskAssignmentSnapshotDtl, String> getBaseMapper() {
		return mapper;
	}

}
