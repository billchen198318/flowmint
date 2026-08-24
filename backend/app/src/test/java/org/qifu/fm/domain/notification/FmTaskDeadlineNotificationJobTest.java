package org.qifu.fm.domain.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.qifu.fm.service.IFmTaskPolicyService;

class FmTaskDeadlineNotificationJobTest {

    @Test
    void resolvesStandaloneParallelTaskPolicyFromParentAndBatch() throws Exception {
        TaskService taskService = mock(TaskService.class);
        TaskQuery query = mock(TaskQuery.class);
        Task task = mock(Task.class);
        IFmTaskPolicyService policyService = mock(IFmTaskPolicyService.class);
        IFmTaskParallelAddSignService batchService =
                mock(IFmTaskParallelAddSignService.class);
        IFmTaskParallelAddSignMemberService memberService =
                mock(IFmTaskParallelAddSignMemberService.class);
        FmNotificationPublisher publisher = mock(FmNotificationPublisher.class);
        FmTaskDeadlineNotificationJob job = new FmTaskDeadlineNotificationJob(
                taskService, policyService, publisher, batchService, memberService);

        when(taskService.createTaskQuery()).thenReturn(query);
        when(query.taskDueBefore(any())).thenReturn(query);
        when(query.orderByTaskDueDate()).thenReturn(query);
        when(query.asc()).thenReturn(query);
        when(query.listPage(0, 500)).thenReturn(List.of(task));
        when(task.getId()).thenReturn("parallel-task");
        when(task.getParentTaskId()).thenReturn("parent-task");
        when(task.getAssignee()).thenReturn("member");
        when(task.getName()).thenReturn("平行加簽");
        when(task.getDueDate()).thenReturn(new Date(System.currentTimeMillis() - 1000));
        when(taskService.getIdentityLinksForTask("parallel-task"))
                .thenReturn(List.of());
        when(taskService.getVariable("parent-task",
                FmTaskAssignmentListener.VARIABLE_TENANT_ID)).thenReturn("tenant");
        when(taskService.getVariable("parent-task",
                FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID)).thenReturn("process-def");
        when(taskService.getVariable("parent-task",
                FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO)).thenReturn(1);
        FmTaskParallelAddSignMember member = new FmTaskParallelAddSignMember();
        member.setParallelAddSignOid("batch");
        when(memberService.findPendingByTask("tenant", "parallel-task"))
                .thenReturn(member);
        FmTaskParallelAddSign batch = new FmTaskParallelAddSign();
        batch.setStatus("WAITING");
        batch.setTaskDefinitionKey("approval");
        when(batchService.selectByPrimaryKey("batch"))
                .thenReturn(result(batch));
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setTenantId("tenant");
        policy.setTaskDefKey("approval");
        when(policyService.findByVersion("tenant", "process-def", 1))
                .thenReturn(List.of(policy));

        job.notifyDeadlines();

        verify(publisher).taskDeadline(
                eq("tenant"), eq("parallel-task"), eq("平行加簽"),
                eq("TASK_OVERDUE"), eq(List.of("member")), any(Date.class));
    }

    private org.qifu.base.model.DefaultResult<FmTaskParallelAddSign> result(
            FmTaskParallelAddSign value) {
        org.qifu.base.model.DefaultResult<FmTaskParallelAddSign> result =
                new org.qifu.base.model.DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
