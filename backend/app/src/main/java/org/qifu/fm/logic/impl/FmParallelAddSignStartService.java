package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.flowable.task.api.Task;
import org.flowable.engine.TaskService;
import org.qifu.base.exception.ServiceException;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.runtime.FmParallelAddSignRequestValidator;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmParallelAddSignStartService {

    public static final String WAITING_VARIABLE = "flowMintParallelAddSignWaiting";

    private final TaskService taskService;
    private final IFmTenantAccountService tenantAccountService;
    private final IFmEmployeeService employeeService;
    private final IFmProcessInstanceService processInstanceService;
    private final IFmFormDataService formDataService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmTaskParallelAddSignService batchService;
    private final IFmTaskParallelAddSignMemberService memberService;
    private final IFmRuntimeAuditLogicService auditLogicService;
    private final FmParallelAddSignRequestValidator validator;
    private final FmNotificationPublisher notificationPublisher;

    public FmParallelAddSignStartService(
            TaskService taskService,
            IFmTenantAccountService tenantAccountService,
            IFmEmployeeService employeeService,
            IFmProcessInstanceService processInstanceService,
            IFmFormDataService formDataService,
            IFmTaskPolicyService taskPolicyService,
            IFmTaskParallelAddSignService batchService,
            IFmTaskParallelAddSignMemberService memberService,
            IFmRuntimeAuditLogicService auditLogicService,
            FmParallelAddSignRequestValidator validator,
            FmNotificationPublisher notificationPublisher) {
        this.taskService = taskService;
        this.tenantAccountService = tenantAccountService;
        this.employeeService = employeeService;
        this.processInstanceService = processInstanceService;
        this.formDataService = formDataService;
        this.taskPolicyService = taskPolicyService;
        this.batchService = batchService;
        this.memberService = memberService;
        this.auditLogicService = auditLogicService;
        this.validator = validator;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public FmTaskParallelAddSign start(
            String tenantId, FmParallelAddSignStartRequest request)
            throws ServiceException {
        String actor = currentAccount(tenantId);
        Task parent = taskService.createTaskQuery()
                .taskId(request.taskId())
                .taskCandidateOrAssigned(actor)
                .singleResult();
        if (parent == null || parent.getParentTaskId() != null
                || parent.getDelegationState() != null) {
            throw new ServiceException("平行加簽只能由目前原始 Task 處理人發起");
        }
        FmProcessInstance process = requiredProcess(
                tenantId, parent.getProcessInstanceId());
        FmTaskPolicy policy = requiredPolicy(process, parent.getTaskDefinitionKey());
        List<String> accounts = validator.validateStart(request, policy, actor);
        FmTaskParallelAddSign repeated = batchService.findByRequestKey(
                tenantId, parent.getId(), request.requestKey().trim());
        if (repeated != null) {
            return repeated;
        }
        if (batchService.findWaitingByParentTask(tenantId, parent.getId()) != null) {
            throw new ServiceException("此 Task 已有進行中的平行加簽");
        }
        Date now = new Date();
        for (String account : accounts) {
            requireActiveEmployee(tenantId, account, now);
        }
        FmFormData formData = requiredFormData(tenantId, process.getFormDataId());
        String snapshotOid = auditLogicService.recordParallelAddSignAction(
                tenantId, process.getProcessInstanceId(), parent.getId(),
                parent.getTaskDefinitionKey(), "PARALLEL_ADD_SIGN_START", "WAITING",
                actor, formData.getOwnerAccount(), null, request.reason().trim(),
                formData, now);
        FmTaskParallelAddSign batch = newBatch(
                tenantId, process, parent, request, actor, accounts.size(),
                snapshotOid, now);
        batchService.insert(batch);
        for (String account : accounts) {
            Task subtask = taskService.newTask();
            subtask.setTenantId(tenantId);
            subtask.setName("平行加簽 - " + parent.getName());
            subtask.setParentTaskId(parent.getId());
            subtask.setAssignee(account);
            subtask.setDueDate(parent.getDueDate());
            subtask.setCategory("FLOWMINT_PARALLEL_ADD_SIGN:" + batch.getOid());
            taskService.saveTask(subtask);
            memberService.insert(newMember(tenantId, batch.getOid(), account,
                    subtask.getId(), actor, now));
            notificationPublisher.parallelAddSignEvent(
                    tenantId, subtask.getId(), batch.getOid() + ":" + subtask.getId(),
                    "PARALLEL_ADD_SIGN_ASSIGNED",
                    List.of(account), actor, now);
        }
        taskService.setVariableLocal(parent.getId(), WAITING_VARIABLE, Boolean.TRUE);
        return batch;
    }

    private FmTaskParallelAddSign newBatch(
            String tenantId, FmProcessInstance process, Task parent,
            FmParallelAddSignStartRequest request, String actor, int total,
            String snapshotOid, Date now) {
        FmTaskParallelAddSign value = new FmTaskParallelAddSign();
        value.setOid(UUID.randomUUID().toString());
        value.setTenantId(tenantId);
        value.setProcessInstanceId(process.getProcessInstanceId());
        value.setParentTaskId(parent.getId());
        value.setTaskDefinitionKey(parent.getTaskDefinitionKey());
        value.setBatchNo(batchService.nextBatchNo(tenantId, parent.getId()));
        value.setRequestKey(request.requestKey().trim());
        value.setStatus("WAITING");
        value.setInitiatorAccount(actor);
        value.setReason(request.reason().trim());
        value.setTotalCount(total);
        value.setCompletedCount(0);
        value.setAgreeCount(0);
        value.setDisagreeCount(0);
        value.setFormSnapshotOid(snapshotOid);
        value.setStartedDate(now);
        value.setLockVersion(0);
        value.setCuserid(actor);
        value.setCdate(now);
        return value;
    }

    private FmTaskParallelAddSignMember newMember(
            String tenantId, String batchOid, String account,
            String taskId, String actor, Date now) {
        FmTaskParallelAddSignMember value = new FmTaskParallelAddSignMember();
        value.setOid(UUID.randomUUID().toString());
        value.setTenantId(tenantId);
        value.setParallelAddSignOid(batchOid);
        value.setMemberAccount(account);
        value.setOriginalMemberAccount(account);
        value.setFlowableTaskId(taskId);
        value.setStatus("PENDING");
        value.setLockVersion(0);
        value.setCuserid(actor);
        value.setCdate(now);
        return value;
    }

    private String currentAccount(String tenantId) throws ServiceException {
        String account = UserUtils.getCurrentUser().getUsername();
        if (!activeMembership(tenantId, account, new Date())) {
            throw new ServiceException("登入帳號不具有效 Tenant membership");
        }
        return account;
    }

    private void requireActiveEmployee(
            String tenantId, String account, Date now) throws ServiceException {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("account", account);
        values.put("status", "ACTIVE");
        boolean active = employeeService.selectListByParams(values).getValue().stream()
                .anyMatch(employee -> effective(employee, now));
        if (!active || !activeMembership(tenantId, account, now)) {
            throw new ServiceException("平行加簽成員不是同 Tenant 有效員工");
        }
    }

    private boolean activeMembership(String tenantId, String account, Date now) {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("account", account);
        values.put("status", "ACTIVE");
        return tenantAccountService.selectListByParams(values).getValue().stream()
                .anyMatch(item -> effective(item.getEffectiveFrom(), item.getEffectiveTo(), now));
    }

    private boolean effective(FmEmployee value, Date now) {
        return effective(value.getEffectiveFrom(), value.getEffectiveTo(), now);
    }

    private boolean effective(Date from, Date to, Date now) {
        return (from == null || !from.after(now)) && (to == null || to.after(now));
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

    private FmTaskPolicy requiredPolicy(FmProcessInstance process, String taskKey)
            throws ServiceException {
        return taskPolicyService.findByVersion(
                process.getTenantId(), process.getProcessDefId(),
                process.getProcessVersionNo()).stream()
                .filter(item -> taskKey.equals(item.getTaskDefKey()))
                .findFirst().orElseThrow(() -> new ServiceException("發布版 Task Policy 不存在"));
    }
}
