package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;

class FmParallelAddSignLifecycleServiceTest {

    private TaskService taskService;
    private IFmTaskParallelAddSignService batchService;
    private IFmTaskParallelAddSignMemberService memberService;
    private FmParallelAddSignLifecycleService service;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        batchService = mock(IFmTaskParallelAddSignService.class);
        memberService = mock(IFmTaskParallelAddSignMemberService.class);
        service = new FmParallelAddSignLifecycleService(
                taskService, batchService, memberService);
    }

    @Test
    void cancelsOnlyPendingMembersOfWaitingBatch() throws Exception {
        FmTaskParallelAddSign batch = batch("batch-1", "WAITING", 3);
        FmTaskParallelAddSignMember pending = member("task-1", "PENDING");
        FmTaskParallelAddSignMember completed = member("task-2", "AGREED");
        TaskQuery query = mock(TaskQuery.class);
        when(batchService.findByProcessInstance("tenant", "process"))
                .thenReturn(List.of(batch));
        when(memberService.findByBatch("tenant", "batch-1"))
                .thenReturn(List.of(pending, completed));
        when(taskService.createTaskQuery()).thenReturn(query);
        when(query.taskId("task-1")).thenReturn(query);
        when(query.singleResult()).thenReturn(mock(Task.class));
        when(memberService.cancelPendingByBatch("tenant", "batch-1"))
                .thenReturn(1);
        when(batchService.cancelWaiting("tenant", "batch-1", 3))
                .thenReturn(1);

        service.cancelWaitingForProcess("tenant", "process", "PROCESS_CANCEL");

        verify(taskService).deleteTask("task-1", "PROCESS_CANCEL");
        verify(taskService, never()).deleteTask("task-2", "PROCESS_CANCEL");
    }

    @Test
    void ignoresCompletedBatch() throws Exception {
        FmTaskParallelAddSign batch = batch("batch-1", "COMPLETED", 4);
        when(batchService.findByProcessInstance("tenant", "process"))
                .thenReturn(List.of(batch));

        service.cancelWaitingForProcess("tenant", "process", "PROCESS_TERMINATED");

        verify(memberService, never()).findByBatch("tenant", "batch-1");
    }

    @Test
    void failsWhenConditionalBatchUpdateLosesRace() {
        FmTaskParallelAddSign batch = batch("batch-1", "WAITING", 3);
        when(batchService.findByProcessInstance("tenant", "process"))
                .thenReturn(List.of(batch));
        when(memberService.findByBatch("tenant", "batch-1"))
                .thenReturn(List.of());
        when(memberService.cancelPendingByBatch("tenant", "batch-1"))
                .thenReturn(0);
        when(batchService.cancelWaiting("tenant", "batch-1", 3))
                .thenReturn(0);

        assertThrows(ServiceException.class, () -> service
                .cancelWaitingForProcess("tenant", "process", "PROCESS_CANCEL"));
    }

    private FmTaskParallelAddSign batch(
            String oid, String status, int lockVersion) {
        FmTaskParallelAddSign batch = new FmTaskParallelAddSign();
        batch.setOid(oid);
        batch.setStatus(status);
        batch.setLockVersion(lockVersion);
        return batch;
    }

    private FmTaskParallelAddSignMember member(String taskId, String status) {
        FmTaskParallelAddSignMember member = new FmTaskParallelAddSignMember();
        member.setFlowableTaskId(taskId);
        member.setStatus(status);
        return member;
    }
}
