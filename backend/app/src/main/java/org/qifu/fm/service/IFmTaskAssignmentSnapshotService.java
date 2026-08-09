package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;

public interface IFmTaskAssignmentSnapshotService
		extends IBaseService<FmTaskAssignmentSnapshot, String> {

	Integer nextResolutionSeq(
			String tenantId, String processInstanceId, String taskDefKey);

	String firstAssignmentSnapshotId(String tenantId, String processInstanceId);

}
