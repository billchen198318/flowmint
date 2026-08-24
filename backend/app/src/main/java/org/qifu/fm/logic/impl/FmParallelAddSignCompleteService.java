package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.runtime.FmParallelAddSignRequestValidator;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmParallelAddSignCompleteService {

    private final TaskService taskService;
    private final IFmTaskParallelAddSignService batchService;
    private final IFmTaskParallelAddSignMemberService memberService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmFormDataService formDataService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final FmParallelAddSignRequestValidator validator;
    private final FmNotificationPublisher notificationPublisher;

    public FmParallelAddSignCompleteService(
            TaskService taskService,
            IFmTaskParallelAddSignService batchService,
            IFmTaskParallelAddSignMemberService memberService,
            IFmProcessInstanceService processInstanceService,
            IFmFormDataService formDataService,
            IFmRuntimeAuditLogicService auditLogicService,
            FmParallelAddSignRequestValidator validator,
            FmNotificationPublisher notificationPublisher) {
        this.taskService = taskService;
        this.batchService = batchService;
        this.memberService = memberService;
        this.processInstanceService = processInstanceService;
        this.formDataService = formDataService;
        this.auditLogicService = auditLogicService;
        this.validator = validator;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public FmTaskActionResultView complete(
            String tenantId, FmParallelAddSignCompleteRequest request)
            throws ServiceException {
        String account = UserUtils.getCurrentUser().getUsername();
        String comment = validator.validateComplete(request, true);
        Task subtask = taskService.createTaskQuery()
                .taskId(request.taskId()).taskAssignee(account).singleResult();
        if (subtask == null || subtask.getParentTaskId() == null) {
            throw new ServiceException("平行加簽 subtask 不存在或不屬於目前操作者");
        }
        FmTaskParallelAddSignMember member = memberService.findPendingByTask(
                tenantId, subtask.getId());
        if (member == null || !account.equals(member.getMemberAccount())) {
            throw new ServiceException("平行加簽成員不存在或已完成");
        }
        FmTaskParallelAddSign batch = batchService
                .selectByPrimaryKey(member.getParallelAddSignOid())
                .getValueEmptyThrowMessage();
        if (!tenantId.equals(batch.getTenantId())
                || !"WAITING".equals(batch.getStatus())
                || !subtask.getParentTaskId().equals(batch.getParentTaskId())) {
            throw new ServiceException("平行加簽批次已結束或 Tenant 不符");
        }
        Task parent = taskService.createTaskQuery()
                .taskId(batch.getParentTaskId()).singleResult();
        if (parent == null) {
            throw new ServiceException("原簽核 Task 已不存在");
        }
        FmProcessInstance process = requiredProcess(tenantId, batch.getProcessInstanceId());
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        String memberStatus = "AGREE".equals(request.result())
                ? "AGREED" : "DISAGREED";
        if (memberService.completePending(
                tenantId, member.getOid(), member.getLockVersion(),
                memberStatus, comment) != 1) {
            throw new ServiceException("平行加簽已由其他請求完成");
        }
        boolean agreed = "AGREED".equals(memberStatus);
        if (batchService.incrementResult(
                tenantId, batch.getOid(), batch.getLockVersion(), agreed) != 1) {
            throw new ServiceException("平行加簽批次已被其他請求更新，請重新整理");
        }
        Date now = new Date();
        taskService.complete(subtask.getId());
        auditLogicService.recordParallelAddSignAction(
                tenantId, process.getProcessInstanceId(), subtask.getId(),
                batch.getTaskDefinitionKey(),
                agreed ? "PARALLEL_ADD_SIGN_AGREE" : "PARALLEL_ADD_SIGN_DISAGREE",
                memberStatus, account, formData.getOwnerAccount(), comment,
                null, formData, now);
        notificationPublisher.parallelAddSignEvent(
                tenantId, subtask.getId(), batch.getOid() + ":" + subtask.getId(),
                "PARALLEL_ADD_SIGN_REPLIED",
                java.util.List.of(batch.getInitiatorAccount()), account, now);
        if (batch.getCompletedCount() + 1 == batch.getTotalCount()) {
            if (batchService.completeWaiting(
                    tenantId, batch.getOid(), batch.getLockVersion() + 1) != 1) {
                throw new ServiceException("平行加簽最後完成狀態發生衝突");
            }
            taskService.setVariableLocal(parent.getId(),
                    FmParallelAddSignStartService.WAITING_VARIABLE, Boolean.FALSE);
            auditLogicService.recordParallelAddSignAction(
                    tenantId, process.getProcessInstanceId(), parent.getId(),
                    batch.getTaskDefinitionKey(), "PARALLEL_ADD_SIGN_COMPLETE",
                    "COMPLETED", account, formData.getOwnerAccount(), null,
                    null, formData, now);
            notificationPublisher.parallelAddSignEvent(
                    tenantId, parent.getId(), batch.getOid(),
                    "PARALLEL_ADD_SIGN_COMPLETED",
                    java.util.List.of(batch.getInitiatorAccount()), account, now);
        }
        return new FmTaskActionResultView(
                subtask.getId(), agreed ? "PARALLEL_ADD_SIGN_AGREE"
                        : "PARALLEL_ADD_SIGN_DISAGREE",
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
