package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;
import org.qifu.fm.mapper.FmTaskAssignmentSnapshotMapper;
import org.qifu.fm.service.IFmTaskAssignmentSnapshotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskAssignmentSnapshotServiceImpl
		extends BaseService<FmTaskAssignmentSnapshot, String>
		implements IFmTaskAssignmentSnapshotService {

	private final FmTaskAssignmentSnapshotMapper mapper;

	public FmTaskAssignmentSnapshotServiceImpl(FmTaskAssignmentSnapshotMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmTaskAssignmentSnapshot, String> getBaseMapper() {
		return mapper;
	}

	@Override
	public Integer nextResolutionSeq(
			String tenantId, String processInstanceId, String taskDefKey) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("tenantId", tenantId);
		paramMap.put("processInstanceId", processInstanceId);
		paramMap.put("taskDefKey", taskDefKey);
		return mapper.selectNextResolutionSeq(paramMap);
	}

	@Override
	public String firstAssignmentSnapshotId(
			String tenantId, String processInstanceId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("tenantId", tenantId);
		paramMap.put("processInstanceId", processInstanceId);
		return mapper.selectFirstAssignmentSnapshotId(paramMap);
	}

}
