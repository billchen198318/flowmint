package org.qifu.fm.mapper;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface FmRuntimeAuditMapper {

    int insertFormSnapshot(Map<String, Object> parameters);

    int insertTaskAction(Map<String, Object> parameters);

    int insertAssignmentSnapshot(Map<String, Object> parameters);

    int insertAssignmentSnapshotDetail(Map<String, Object> parameters);

    int selectNextResolutionSeq(
            @Param("tenantId") String tenantId,
            @Param("processInstanceId") String processInstanceId,
            @Param("taskDefKey") String taskDefKey);

    String selectFirstAssignmentSnapshotId(
            @Param("tenantId") String tenantId,
            @Param("processInstanceId") String processInstanceId);
}
