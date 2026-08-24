package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmTaskParallelAddSign;

public interface IFmTaskParallelAddSignService
        extends IBaseService<FmTaskParallelAddSign, String> {

    FmTaskParallelAddSign findWaitingByParentTask(
            String tenantId, String parentTaskId);

    FmTaskParallelAddSign findByRequestKey(
            String tenantId, String parentTaskId, String requestKey);

    FmTaskParallelAddSign findLatestByParentTask(
            String tenantId, String parentTaskId);

    List<FmTaskParallelAddSign> findByProcessInstance(
            String tenantId, String processInstanceId);

    Integer nextBatchNo(String tenantId, String parentTaskId);

    int incrementResult(
            String tenantId, String oid, int lockVersion, boolean agreed);

    int completeWaiting(String tenantId, String oid, int lockVersion);

    int cancelWaiting(String tenantId, String oid, int lockVersion);
}
