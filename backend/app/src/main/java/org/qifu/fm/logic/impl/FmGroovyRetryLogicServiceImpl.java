package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.dto.command.FmGroovyRetryCommand;
import org.qifu.fm.dto.view.FmGroovyRetryView;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.IFmGroovyRetryLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(transactionManager = "transactionManager", readOnly = true, rollbackFor = Exception.class)
public class FmGroovyRetryLogicServiceImpl implements IFmGroovyRetryLogicService {

    private final FmGroovyOperateAccess access;
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskIncidentActionService actions;
    private final IFmProcessInstanceService processes;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final FmGroovyRuntimeRunner runner;
    private final FmGroovyRetryJob jobs;
    private final boolean enabled;

    public FmGroovyRetryLogicServiceImpl(FmGroovyOperateAccess access, IFmSystemTaskExecutionService executions,
            IFmSystemTaskIncidentActionService actions, IFmProcessInstanceService processes,
            IFmGroovyRuntimeInputLogicService inputs, FmGroovyRuntimeRunner runner, FmGroovyRetryJob jobs,
            @Value("${flowmint.groovy.manual-retry.enabled:false}") boolean enabled,
            @Value("${flowmint.groovy.incident-handling.enabled:false}") boolean handlingEnabled) {
        this.access = access;
        this.executions = executions;
        this.actions = actions;
        this.processes = processes;
        this.inputs = inputs;
        this.runner = runner;
        this.jobs = jobs;
        this.enabled = enabled && handlingEnabled;
    }

    @Override
    public DefaultResult<FmGroovyRetryView> preview(String tenantId, String invocationId) throws ServiceException {
        authorize(tenantId);
        var row = observed(tenantId, invocationId);
        var history = actions.history(tenantId, row.getOid(), 0, 1);
        int revision = revision(tenantId, row.getOid(), history);
        eligible(row, history);
        var processRows = processes.selectListByParams(Map.of("tenantId", tenantId,
                "processInstanceId", row.getProcessInstanceId())).getValue();
        require(processRows != null && processRows.size() == 1);
        var process = processRows.getFirst();
        require(process != null && tenantId.equals(process.getTenantId())
                && row.getProcessInstanceId().equals(process.getProcessInstanceId())
                && "RUNNING".equals(process.getInstanceStatus()));
        checkInput(row);
        jobs.check(row, process.getFlowableProcessDefId());
        return success(new FmGroovyRetryView(invocationId, revision, row.getGeneration(), row.getInputRevisionNo(),
                row.getInputSha256(), row.getBindingSha256(), 3 - row.getAttemptNo()));
    }

    @Override
    @Transactional(transactionManager = "transactionManager", readOnly = false, rollbackFor = Exception.class, timeout = 30)
    public DefaultResult<Boolean> retry(String tenantId, FmGroovyRetryCommand command) throws ServiceException {
        authorize(tenantId);
        require(command != null && id(command.requestId()) && command.expectedRevision() >= 0
                && command.expectedRevision() < Integer.MAX_VALUE && command.expectedGeneration() > 0
                && command.reason() != null && !command.reason().isBlank() && command.reason().trim().length() <= 1000);
        var user = UserUtils.getCurrentUser();
        require(user != null && user.getUserId() != null && !user.getUserId().isBlank()
                && user.getUserId().length() <= 24);
        String actor = user.getUserId();
        var observed = observed(tenantId, command.invocationId());
        // Shared lock order serializes cancellation, handling and retry acceptance.
        var process = processes.lockInstance(tenantId, observed.getProcessInstanceId());
        require(process != null && tenantId.equals(process.getTenantId())
                && observed.getProcessInstanceId().equals(process.getProcessInstanceId()));
        var row = executions.lockInvocation(tenantId, command.invocationId());
        require(row != null && tenantId.equals(row.getTenantId()) && observed.getOid().equals(row.getOid())
                && observed.getProcessInstanceId().equals(row.getProcessInstanceId())
                && command.invocationId().equals(row.getInvocationId()) && "GROOVY".equals(row.getTaskType()));
        var previous = actions.lockHistory(tenantId, row.getOid(), command.requestId());
        revision(tenantId, row.getOid(), previous);
        if (!previous.isEmpty()) {
            var action = previous.getFirst();
            require(command.requestId().equals(action.getRequestId()) && "RETRY".equals(action.getActionType())
                    && actor.equals(action.getActor()) && command.reason().trim().equals(action.getReason())
                    && action.getActionNo() == command.expectedRevision() + 1
                    && Integer.valueOf(command.expectedGeneration()).equals(action.getExpectedGeneration()));
            return success(true); // Accepted previously, even if the worker has since completed or failed.
        }
        var latest = actions.lockHistory(tenantId, row.getOid(), null);
        require(revision(tenantId, row.getOid(), latest) == command.expectedRevision()
                && Integer.valueOf(command.expectedGeneration()).equals(row.getGeneration())
                && "RUNNING".equals(process.getInstanceStatus()));
        eligible(row, latest);
        checkInput(row);
        var action = new FmSystemTaskIncidentAction();
        action.setTenantId(tenantId);
        action.setExecutionOid(row.getOid());
        action.setRequestId(command.requestId());
        action.setActionNo(command.expectedRevision() + 1);
        action.setActionType("RETRY");
        action.setExpectedGeneration(row.getGeneration());
        action.setFromStatus("OPEN");
        action.setToStatus("OPEN");
        action.setReason(command.reason().trim());
        action.setActor(actor);
        action.setActionDate(new Date());
        actions.insert(action).getValueEmptyThrowMessage();
        require(executions.rearm(tenantId, row.getOid(), row.getGeneration()) == 1);
        jobs.restore(row, process.getFlowableProcessDefId());
        return success(true);
    }

    private void checkInput(FmSystemTaskExecution row) throws ServiceException {
        require(row.getStartedAt() != null);
        var loaded = inputs.load(row.getTenantId(), row.getFlowableExecutionId(), row.getInvocationId(),
                row.getStartedAt().toInstant(), runner.profile());
        require(Objects.equals(row.getTenantId(), loaded.tenantId())
                && Objects.equals(row.getFlowableExecutionId(), loaded.executionId())
                && Objects.equals(row.getProcessInstanceId(), loaded.processInstanceId())
                && Objects.equals(row.getProcessDefId(), loaded.processDefId())
                && Objects.equals(row.getVersionNo(), loaded.processVersionNo())
                && Objects.equals(row.getNodeId(), loaded.nodeId())
                && Objects.equals(row.getFormDataId(), loaded.formDataId())
                && Objects.equals(row.getInputRevisionNo(), loaded.snapshot().revisionNo())
                && Objects.equals(row.getExpectedFormLock(), loaded.expectedFormLock())
                && Objects.equals(row.getInputContent(), loaded.snapshot().inputJson())
                && Objects.equals(row.getContextContent(), loaded.snapshot().contextJson())
                && Objects.equals(row.getInputSha256(), loaded.snapshot().sha256())
                && Objects.equals(row.getBindingSha256(), loaded.preparation().bindingSha256())
                && Objects.equals(row.getManifestSha256(), loaded.preparation().manifestSha256()));
    }

    private void eligible(FmSystemTaskExecution row, List<FmSystemTaskIncidentAction> latest) throws ServiceException {
        require("FAILED".equals(row.getStatus()) && row.getGeneration() != null && row.getGeneration() > 0
                && row.getGeneration() < Integer.MAX_VALUE - 1 && row.getAttemptNo() != null
                && row.getAttemptNo() >= 0 && row.getAttemptNo() < 3 && row.getLeaseUntil() == null
                && row.getCompletedAt() != null && row.getResultSha256() == null && row.getFormSnapshotOid() == null
                && (latest.isEmpty() || "OPEN".equals(latest.getFirst().getToStatus())));
    }

    private FmSystemTaskExecution observed(String tenantId, String invocationId) throws ServiceException {
        require(id(invocationId));
        var rows = executions.selectListByParams(Map.of("tenantId", tenantId, "invocationId", invocationId)).getValue();
        require(rows != null && rows.size() == 1 && rows.getFirst() != null);
        var row = rows.getFirst();
        require(tenantId.equals(row.getTenantId()) && invocationId.equals(row.getInvocationId())
                && "GROOVY".equals(row.getTaskType()) && row.getOid() != null && row.getProcessInstanceId() != null);
        return row;
    }

    private int revision(String tenantId, String executionOid, List<FmSystemTaskIncidentAction> values)
            throws ServiceException {
        require(values != null && values.size() <= 1);
        if (values.isEmpty()) return 0;
        var value = values.getFirst();
        require(value != null && tenantId.equals(value.getTenantId()) && executionOid.equals(value.getExecutionOid())
                && value.getActionNo() != null && value.getActionNo() > 0);
        return value.getActionNo();
    }

    private void authorize(String tenantId) throws ServiceException {
        access.require(tenantId);
        if (!enabled) throw new ServiceException("GROOVY_RETRY_UNAVAILABLE");
    }

    private boolean id(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}");
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) throw new ServiceException("GROOVY_RETRY_INVALID");
    }

    private <T> DefaultResult<T> success(T value) {
        var result = new DefaultResult<T>();
        result.setValue(value);
        result.setSuccess(YesNoKeyProvide.YES);
        return result;
    }
}
