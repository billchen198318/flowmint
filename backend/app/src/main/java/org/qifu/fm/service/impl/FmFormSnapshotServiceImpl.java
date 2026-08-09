package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.mapper.FmFormSnapshotMapper;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmFormSnapshotServiceImpl extends BaseService<FmFormSnapshot, String>
		implements IFmFormSnapshotService {

	private final FmFormSnapshotMapper mapper;

	public FmFormSnapshotServiceImpl(FmFormSnapshotMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmFormSnapshot, String> getBaseMapper() {
		return mapper;
	}

}
