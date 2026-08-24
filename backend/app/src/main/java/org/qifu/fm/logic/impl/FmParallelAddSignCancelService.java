package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmParallelAddSignCancelRequest;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmParallelAddSignCancelService {

    private final TaskService taskService;
    private final IFmTaskParallelAddSignService batchService;
    private final IFmTaskParallelAddSignMemberService memberService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmFormDataService formDataService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final FmNotificationPublisher notificationPublisher;

    public FmParallelAddSignCancelService(
            TaskService taskService,
            IFmTaskParallelAddSignService batchService,
            IFmTaskParallelAddSignMemberService memberService,
            IFmProcessInstanceService processInstanceService,
            IFmFormDataService formDataService,
            IFmRuntimeAuditLogicService auditLogicService,
            FmNotificationPublisher notificationPublisher) {
        this.taskService = taskService;
        this.batchService = batchService;
        this.memberService = memberService;
        this.processInstanceService = processInstanceService;
        this.formDataService = formDataService;
        this.auditLogicService = auditLogicService;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public FmTaskActionResultView cancel(
            String tenantId, FmParallelAddSignCancelRequest request)
            throws ServiceException {
        if (request == null || StringUtils.isAnyBlank(
                request.taskId(), request.reason())) {
            throw new ServiceException("平行加簽 Parent Task 與取消原因必填");
        }
        String reason = request.reason().trim();
        if (reason.length() > 1000) {
            throw new ServiceException("平行加簽取消原因不得超過 1000 字");
        }
        String account = UserUtils.getCurrentUser().getUsername();
        FmTaskParallelAddSign batch = batchService.findWaitingByParentTask(
                tenantId, request.taskId());
        if (batch == null || !account.equals(batch.getInitiatorAccount())
                || batch.getCompletedCount() != 0) {
            throw new ServiceException("只有發起人可在尚無回覆時取消平行加簽");
        }
        Task parent = taskService.createTaskQuery()
                .taskId(batch.getParentTaskId()).taskAssignee(account).singleResult();
        if (parent == null) {
            throw new ServiceException("原簽核 Task 不存在或不屬於目前操作者");
        }
        FmProcessInstance process = requiredProcess(tenantId, batch.getProcessInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        var members = memberService.findByBatch(tenantId, batch.getOid());
        members.stream()
                .filter(member -> "PENDING".equals(member.getStatus()))
                .map(member -> member.getFlowableTaskId())
                .filter(StringUtils::isNotBlank)
                .forEach(taskId -> {
                    if (taskService.createTaskQuery().taskId(taskId).singleResult() != null) {
                        taskService.deleteTask(taskId, "PARALLEL_ADD_SIGN_CANCELLED");
                    }
                });
        int cancelledMembers = memberService.cancelPendingByBatch(
                tenantId, batch.getOid());
        if (cancelledMembers != batch.getTotalCount()
                || batchService.cancelWaiting(
                        tenantId, batch.getOid(), batch.getLockVersion()) != 1) {
            throw new ServiceException("平行加簽取消發生併發衝突");
        }
        taskService.setVariableLocal(parent.getId(),
                FmParallelAddSignStartService.WAITING_VARIABLE, Boolean.FALSE);
        Date now = new Date();
        auditLogicService.recordParallelAddSignAction(
                tenantId, process.getProcessInstanceId(), parent.getId(),
                batch.getTaskDefinitionKey(), "PARALLEL_ADD_SIGN_CANCEL",
                "CANCELLED", account, formData.getOwnerAccount(), null,
                reason, formData, now);
        java.util.LinkedHashSet<String> recipients = new java.util.LinkedHashSet<>();
        recipients.add(batch.getInitiatorAccount());
        members.forEach(member -> {
            recipients.add(member.getOriginalMemberAccount());
            recipients.add(member.getMemberAccount());
        });
        notificationPublisher.parallelAddSignEvent(
                tenantId, parent.getId(), batch.getOid(),
                "PARALLEL_ADD_SIGN_CANCELLED",
                recipients, account, now);
        return new FmTaskActionResultView(
                parent.getId(), "PARALLEL_ADD_SIGN_CANCEL",
                process.getProcessInstanceId(), "RUNNING");
    }

    private FmProcessInstance requiredProcess(String tenantId, String processId)
            throws ServiceException {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("processInstanceId", processId);
        return processInstanceService.selectListByParams(values).getValue().stream()
                .filter(item -> "RUNNING".equals(item.getInstanceStatus()))
                .findFirst().orElseThrow(() -> new ServiceException("流程不存在或已結束"));
    }

    private FmFormData requiredFormData(String tenantId, String formDataId)
            throws ServiceException {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("formDataId", formDataId);
        return formDataService.selectListByParams(values).getValue().stream()
                .findFirst().orElseThrow(() -> new ServiceException("表單資料不存在"));
    }
}
