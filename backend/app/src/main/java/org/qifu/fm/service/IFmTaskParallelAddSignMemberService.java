package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;

public interface IFmTaskParallelAddSignMemberService
        extends IBaseService<FmTaskParallelAddSignMember, String> {

    List<FmTaskParallelAddSignMember> findByBatch(
            String tenantId, String parallelAddSignOid);

    FmTaskParallelAddSignMember findPendingByTask(
            String tenantId, String flowableTaskId);

    int completePending(
            String tenantId, String oid, int lockVersion,
            String status, String comment);

    int cancelPendingByBatch(String tenantId, String parallelAddSignOid);

    int reassignPending(
            String tenantId, String oid, int lockVersion, String targetAccount,
            String actor);
}
