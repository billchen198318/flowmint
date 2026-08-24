package org.qifu.fm.domain.notification;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FmTaskDeadlineNotificationJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            FmTaskDeadlineNotificationJob.class);
    private static final int PAGE_SIZE = 500;

    private final TaskService taskService;
    private final IFmTaskPolicyService taskPolicyService;
    private final FmNotificationPublisher notificationPublisher;
    private final IFmTaskParallelAddSignService parallelBatchService;
    private final IFmTaskParallelAddSignMemberService parallelMemberService;

    public FmTaskDeadlineNotificationJob(TaskService taskService,
            IFmTaskPolicyService taskPolicyService,
            FmNotificationPublisher notificationPublisher,
            IFmTaskParallelAddSignService parallelBatchService,
            IFmTaskParallelAddSignMemberService parallelMemberService) {
        this.taskService = taskService;
        this.taskPolicyService = taskPolicyService;
        this.notificationPublisher = notificationPublisher;
        this.parallelBatchService = parallelBatchService;
        this.parallelMemberService = parallelMemberService;
    }

    @Scheduled(initialDelay = 90000, fixedDelay = 300000)
    public void notifyDeadlines() {
        Date now = new Date();
        Date horizon = Date.from(now.toInstant().plus(8760, ChronoUnit.HOURS));
        int offset = 0;
        List<Task> tasks;
        do {
            tasks = taskService.createTaskQuery().taskDueBefore(horizon)
                    .orderByTaskDueDate().asc().listPage(offset, PAGE_SIZE);
            tasks.forEach(task -> notifyTask(task, now));
            offset += tasks.size();
        } while (tasks.size() == PAGE_SIZE);
    }

    private void notifyTask(Task task, Date now) {
        try {
            FmTaskPolicy policy = policy(task);
            if (policy == null || task.getDueDate() == null) {
                return;
            }
            String eventType;
            if (!task.getDueDate().after(now)) {
                eventType = "TASK_OVERDUE";
            } else if (policy.getReminderBeforeHours() != null
                    && !Date.from(task.getDueDate().toInstant().minus(
                            policy.getReminderBeforeHours(), ChronoUnit.HOURS)).after(now)) {
                eventType = "TASK_DUE_SOON";
            } else {
                return;
            }
            notificationPublisher.taskDeadline(policy.getTenantId(), task.getId(),
                    task.getName(), eventType, recipients(task), now);
        } catch (Exception exception) {
            LOGGER.warn("FlowMint task deadline notification failed for task {}: {}",
                    task.getId(), exception.getMessage());
        }
    }

    private FmTaskPolicy policy(Task task) {
        String tenantId = variable(task, FmTaskAssignmentListener.VARIABLE_TENANT_ID);
        String processDefId = variable(task,
                FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID);
        String version = variable(task,
                FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO);
        if (StringUtils.isAnyBlank(tenantId, processDefId, version)) {
            return null;
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        if (StringUtils.isBlank(taskDefinitionKey)
                && StringUtils.isNotBlank(task.getParentTaskId())) {
            var member = parallelMemberService.findPendingByTask(tenantId, task.getId());
            if (member != null) {
                var batch = parallelBatchService
                        .selectByPrimaryKey(member.getParallelAddSignOid()).getValue();
                if (batch != null && "WAITING".equals(batch.getStatus())) {
                    taskDefinitionKey = batch.getTaskDefinitionKey();
                }
            }
        }
        final String policyTaskKey = taskDefinitionKey;
        return taskPolicyService.findByVersion(tenantId, processDefId,
                Integer.valueOf(version)).stream()
                .filter(value -> policyTaskKey != null
                        && policyTaskKey.equals(value.getTaskDefKey()))
                .findFirst().orElse(null);
    }

    private String variable(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        if (value == null && StringUtils.isNotBlank(task.getParentTaskId())) {
            value = taskService.getVariable(task.getParentTaskId(), name);
        }
        return value == null ? null : value.toString();
    }

    private List<String> recipients(Task task) {
        Set<String> accounts = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(task.getAssignee())) {
            accounts.add(task.getAssignee());
        }
        for (IdentityLink link : taskService.getIdentityLinksForTask(task.getId())) {
            if ("candidate".equals(link.getType()) && StringUtils.isNotBlank(link.getUserId())) {
                accounts.add(link.getUserId());
            }
        }
        return new ArrayList<>(accounts);
    }
}
