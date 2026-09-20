package org.qifu.fm.service.impl;

import java.util.UUID;

import org.qifu.base.exception.ServiceException;
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

	@Override
	@Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
	public String insertSystemTaskSnapshot(FmFormSnapshot snapshot) throws ServiceException {
		if (snapshot == null || !"SYSTEM_TASK_APPLY".equals(snapshot.getActionType())
				|| snapshot.getTenantId() == null || snapshot.getTenantId().isBlank()
				|| snapshot.getFormDataId() == null || snapshot.getProcessInstanceId() == null
				|| snapshot.getFormVersionNo() == null || snapshot.getFormVersionNo() < 1
				|| snapshot.getRevisionNo() == null || snapshot.getRevisionNo() < 1
				|| snapshot.getDataContent() == null || snapshot.getSnapshotDate() == null
				|| snapshot.getContentSha256() == null
				|| !snapshot.getContentSha256().matches("[a-f0-9]{64}")) {
			throw new ServiceException("SYSTEM_TASK_SNAPSHOT_INVALID");
		}
		// BaseService.insert overwrites CUSERID with the web principal (null in worker threads).
		// This internal insert preserves the explicit system actor without impersonating a user.
		snapshot.setOid(UUID.randomUUID().toString());
		snapshot.setFormSnapshotId(UUID.randomUUID().toString());
		snapshot.setTaskId(null);
		snapshot.setCuserid("SYSTEM_TASK");
		snapshot.setCdate(snapshot.getSnapshotDate());
		snapshot.setUuserid(null);
		snapshot.setUdate(null);
		if (mapper.insert(snapshot) != 1) {
			throw new ServiceException("SYSTEM_TASK_SNAPSHOT_INSERT_FAILED");
		}
		return snapshot.getOid();
	}

}
