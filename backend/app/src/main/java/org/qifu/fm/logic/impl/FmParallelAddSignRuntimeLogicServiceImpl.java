package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmParallelAddSignCancelRequest;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmParallelAddSignDetailView;
import org.qifu.fm.dto.view.FmParallelAddSignMemberView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.flowable.FmTaskAssignmentListener;
import org.qifu.fm.logic.IFmParallelAddSignRuntimeLogicService;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmParallelAddSignRuntimeLogicServiceImpl
        implements IFmParallelAddSignRuntimeLogicService {

    private final TaskService taskService;
    private final IFmEmployeeService employeeService;
    private final IFmTenantAccountService tenantAccountService;
    private final IFmTaskPolicyService taskPolicyService;
    private final IFmTaskParallelAddSignService batchService;
    private final IFmTaskParallelAddSignMemberService memberService;
    private final FmParallelAddSignStartService startService;
    private final FmParallelAddSignCompleteService completeService;
    private final FmParallelAddSignCancelService cancelService;

    public FmParallelAddSignRuntimeLogicServiceImpl(
            TaskService taskService,
            IFmEmployeeService employeeService,
            IFmTenantAccountService tenantAccountService,
            IFmTaskPolicyService taskPolicyService,
            IFmTaskParallelAddSignService batchService,
            IFmTaskParallelAddSignMemberService memberService,
            FmParallelAddSignStartService startService,
            FmParallelAddSignCompleteService completeService,
            FmParallelAddSignCancelService cancelService) {
        this.taskService = taskService;
        this.employeeService = employeeService;
        this.tenantAccountService = tenantAccountService;
        this.taskPolicyService = taskPolicyService;
        this.batchService = batchService;
        this.memberService = memberService;
        this.startService = startService;
        this.completeService = completeService;
        this.cancelService = cancelService;
    }

    @Override
    public DefaultResult<List<FmOptionView>> options(
            String tenantId, String taskId) throws ServiceException {
        String actor = currentAccount(tenantId);
        Task task = authorizedParent(taskId, actor);
        FmTaskPolicy policy = taskPolicyService.findByVersion(
                tenantId,
                (String) taskService.getVariable(
                        task.getId(), FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID),
                (Integer) taskService.getVariable(
                        task.getId(), FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO))
                .stream().filter(item -> task.getTaskDefinitionKey()
                        .equals(item.getTaskDefKey())).findFirst()
                .orElseThrow(() -> new ServiceException("發布版 Task Policy 不存在"));
        if (!"Y".equals(policy.getAllowParallelAddSign())) {
            throw new ServiceException("此關卡未允許平行加簽");
        }
        Date now = new Date();
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("status", "ACTIVE");
        return success(employeeService.selectListByParams(
                values, "EMPLOYEE_NO", "ASC").getValue().stream()
                .filter(item -> !actor.equals(item.getAccount()))
                .filter(item -> effective(item.getEffectiveFrom(), item.getEffectiveTo(), now))
                .filter(item -> activeMembership(tenantId, item.getAccount(), now))
                .map(item -> new FmOptionView(item.getAccount(),
                        item.getEmployeeNo() + " - " + item.getDisplayName()))
                .toList());
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmParallelAddSignDetailView> start(
            String tenantId, FmParallelAddSignStartRequest request)
            throws ServiceException {
        return success(detailView(tenantId, startService.start(tenantId, request)));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> complete(
            String tenantId, FmParallelAddSignCompleteRequest request)
            throws ServiceException {
        return success(completeService.complete(tenantId, request));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmTaskActionResultView> cancel(
            String tenantId, FmParallelAddSignCancelRequest request)
            throws ServiceException {
        return success(cancelService.cancel(tenantId, request));
    }

    @Override
    public DefaultResult<FmParallelAddSignDetailView> detail(
            String tenantId, String taskId) throws ServiceException {
        String actor = currentAccount(tenantId);
        FmTaskParallelAddSign batch = batchService.findWaitingByParentTask(
                tenantId, taskId);
        if (batch == null) {
            batch = batchService.findLatestByParentTask(tenantId, taskId);
        }
        if (batch == null) {
            var member = memberService.findPendingByTask(tenantId, taskId);
            if (member != null) {
                batch = batchService.selectByPrimaryKey(member.getParallelAddSignOid())
                        .getValueEmptyThrowMessage();
            }
        }
        if (batch == null || !tenantId.equals(batch.getTenantId())) {
            throw new ServiceException("平行加簽批次不存在");
        }
        boolean parentActor = batch.getInitiatorAccount().equals(actor);
        boolean memberActor = memberService.findByBatch(tenantId, batch.getOid()).stream()
                .anyMatch(item -> actor.equals(item.getMemberAccount()));
        if (!parentActor && !memberActor) {
            throw new ServiceException("目前帳號無權查看此平行加簽");
        }
        return success(detailView(tenantId, batch));
    }

    @Override
    public DefaultResult<List<FmParallelAddSignDetailView>> processDetails(
            String tenantId, String processInstanceId) throws ServiceException {
        if (StringUtils.isAnyBlank(tenantId, processInstanceId)) {
            throw new ServiceException("Tenant 與流程實例不可為空");
        }
        return success(batchService.findByProcessInstance(tenantId, processInstanceId)
                .stream().map(batch -> detailView(tenantId, batch)).toList());
    }

    private FmParallelAddSignDetailView detailView(
            String tenantId, FmTaskParallelAddSign batch) {
        List<FmParallelAddSignMemberView> members = memberService
                .findByBatch(tenantId, batch.getOid()).stream()
                .map(item -> new FmParallelAddSignMemberView(
                        item.getMemberAccount(), displayName(tenantId, item.getMemberAccount()),
                        item.getOriginalMemberAccount(),
                        displayName(tenantId, item.getOriginalMemberAccount()),
                        item.getFlowableTaskId(), item.getStatus(), item.getComment(),
                        item.getCompletedDate())).toList();
        return new FmParallelAddSignDetailView(
                batch.getOid(), batch.getParentTaskId(), batch.getStatus(),
                batch.getInitiatorAccount(), batch.getReason(), batch.getTotalCount(),
                batch.getCompletedCount(), batch.getAgreeCount(), batch.getDisagreeCount(),
                batch.getStartedDate(), batch.getCompletedDate(), batch.getCancelledDate(),
                "WAITING".equals(batch.getStatus()) && batch.getCompletedCount() == 0,
                members);
    }

    private Task authorizedParent(String taskId, String actor) throws ServiceException {
        Task task = taskService.createTaskQuery().taskId(taskId)
                .taskCandidateOrAssigned(actor).singleResult();
        if (task == null || task.getParentTaskId() != null) {
            throw new ServiceException("Task 不存在或目前帳號無權操作");
        }
        return task;
    }

    private String currentAccount(String tenantId) throws ServiceException {
        String actor = UserUtils.getCurrentUser().getUsername();
        if (!activeMembership(tenantId, actor, new Date())) {
            throw new ServiceException("登入帳號不具有效 Tenant membership");
        }
        return actor;
    }

    private boolean activeMembership(String tenantId, String account, Date now) {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("account", account);
        values.put("status", "ACTIVE");
        return tenantAccountService.selectListByParams(values).getValue().stream()
                .anyMatch(item -> effective(item.getEffectiveFrom(), item.getEffectiveTo(), now));
    }

    private String displayName(String tenantId, String account) {
        Map<String, Object> values = new HashMap<>();
        values.put("tenantId", tenantId);
        values.put("account", account);
        return employeeService.selectListByParams(values).getValue().stream()
                .map(FmEmployee::getDisplayName).findFirst().orElse(account);
    }

    private boolean effective(Date from, Date to, Date now) {
        return (from == null || !from.after(now)) && (to == null || to.after(now));
    }

    private <T> DefaultResult<T> success(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
