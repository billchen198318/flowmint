package org.qifu.fm.logic.impl;

import java.util.List;

import org.flowable.engine.TaskService;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmParallelAddSignLifecycleService {

    private final TaskService taskService;
    private final IFmTaskParallelAddSignService batchService;
    private final IFmTaskParallelAddSignMemberService memberService;

    public FmParallelAddSignLifecycleService(
            TaskService taskService,
            IFmTaskParallelAddSignService batchService,
            IFmTaskParallelAddSignMemberService memberService) {
        this.taskService = taskService;
        this.batchService = batchService;
        this.memberService = memberService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelWaitingForProcess(
            String tenantId, String processInstanceId, String deleteReason)
            throws ServiceException {
        List<FmTaskParallelAddSign> batches = batchService
                .findByProcessInstance(tenantId, processInstanceId).stream()
                .filter(batch -> "WAITING".equals(batch.getStatus()))
                .toList();
        for (FmTaskParallelAddSign batch : batches) {
            List<FmTaskParallelAddSignMember> pendingMembers = memberService
                    .findByBatch(tenantId, batch.getOid()).stream()
                    .filter(member -> "PENDING".equals(member.getStatus()))
                    .toList();
            for (FmTaskParallelAddSignMember member : pendingMembers) {
                if (taskService.createTaskQuery()
                        .taskId(member.getFlowableTaskId()).singleResult() != null) {
                    taskService.deleteTask(member.getFlowableTaskId(), deleteReason);
                }
            }
            if (memberService.cancelPendingByBatch(tenantId, batch.getOid())
                    != pendingMembers.size()
                    || batchService.cancelWaiting(
                            tenantId, batch.getOid(), batch.getLockVersion()) != 1) {
                throw new ServiceException("平行加簽狀態已變更，請重新整理後再試");
            }
        }
    }
}
