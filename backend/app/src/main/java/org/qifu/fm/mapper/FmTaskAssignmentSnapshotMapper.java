package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.fm.entity.FmTaskAssignmentSnapshot;

@Mapper
public interface FmTaskAssignmentSnapshotMapper
		extends IBaseMapper<FmTaskAssignmentSnapshot, String> {

	public Integer selectNextResolutionSeq(Map<String, Object> paramMap);

	public String selectFirstAssignmentSnapshotId(Map<String, Object> paramMap);

}
