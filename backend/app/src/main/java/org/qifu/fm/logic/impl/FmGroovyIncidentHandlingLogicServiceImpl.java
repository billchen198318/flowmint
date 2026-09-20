package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.dto.command.FmGroovyIncidentHandleCommand;
import org.qifu.fm.dto.view.FmGroovyIncidentHandlingView;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.IFmGroovyIncidentHandlingLogicService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmGroovyIncidentHandlingLogicServiceImpl implements IFmGroovyIncidentHandlingLogicService {

    private final FmGroovyOperateAccess access;
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskIncidentActionService actions;
    private final IFmProcessInstanceService processes;
    private final boolean enabled;

    public FmGroovyIncidentHandlingLogicServiceImpl(FmGroovyOperateAccess access,
            IFmSystemTaskExecutionService executions, IFmSystemTaskIncidentActionService actions,
            IFmProcessInstanceService processes,
            @Value("${flowmint.groovy.incident-handling.enabled:false}") boolean enabled) {
        this.access = access;
        this.executions = executions;
        this.actions = actions;
        this.processes = processes;
        this.enabled = enabled;
    }

    @Override
    public DefaultResult<FmGroovyIncidentHandlingView> load(String tenantId, String invocationId, int offset)
            throws ServiceException {
        authorize(tenantId);
        require(offset >= 0 && offset <= 1000000 && offset % 50 == 0);
        var row = incident(tenantId, invocationId);
        var latest = actions.history(tenantId, row.getOid(), 0, 1);
        var history = actions.history(tenantId, row.getOid(), offset, 51);
        validateActions(tenantId, row.getOid(), latest);
        validateActions(tenantId, row.getOid(), history);
        require(latest.size() <= 1 && history.size() <= 51);
        return success(view(latest, history, offset));
    }

    @Override
    @Transactional(transactionManager = "transactionManager", readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmGroovyIncidentHandlingView> handle(String tenantId, FmGroovyIncidentHandleCommand command)
            throws ServiceException {
        authorize(tenantId);
        require(command != null && id(command.invocationId()) && id(command.requestId())
                && command.expectedRevision() >= 0 && command.expectedRevision() < Integer.MAX_VALUE
                && command.targetStatus() != null && Set.of("OPEN", "RESOLVED", "IGNORED").contains(command.targetStatus())
                && command.reason() != null && !command.reason().isBlank() && command.reason().trim().length() <= 1000);
        String actor = UserUtils.getCurrentUser().getUserId();
        require(actor != null && !actor.isBlank() && actor.length() <= 24);
        var observed = incident(tenantId, command.invocationId());
        // Same lock order as claim/cancellation. Never alter engine or execution status here.
        var process = processes.lockInstance(tenantId, observed.getProcessInstanceId());
        require(process != null && tenantId.equals(process.getTenantId())
                && observed.getProcessInstanceId().equals(process.getProcessInstanceId()));
        var row = executions.lockInvocation(tenantId, command.invocationId());
        require(row != null && observed.getOid().equals(row.getOid()) && tenantId.equals(row.getTenantId())
                && observed.getProcessInstanceId().equals(row.getProcessInstanceId())
                && command.invocationId().equals(row.getInvocationId())
                && "GROOVY".equals(row.getTaskType()) && manageable(row));
        var previousRequest = actions.lockHistory(tenantId, row.getOid(), command.requestId());
        validateActions(tenantId, row.getOid(), previousRequest);
        require(previousRequest.size() <= 1);
        if (!previousRequest.isEmpty()) {
            var previous = previousRequest.getFirst();
            require("STATUS".equals(previous.getActionType())
                    && command.requestId().equals(previous.getRequestId()) && actor.equals(previous.getActor())
                    && previous.getActionNo() == command.expectedRevision() + 1
                    && command.targetStatus().equals(previous.getToStatus())
                    && command.reason().trim().equals(previous.getReason()));
            // Return the current state using locking reads, even when this request is an older replay.
            return success(current(tenantId, row.getOid()));
        }
        var latest = actions.lockHistory(tenantId, row.getOid(), null);
        validateActions(tenantId, row.getOid(), latest);
        require(latest.size() <= 1);
        int revision = latest.isEmpty() ? 0 : latest.getFirst().getActionNo();
        String from = latest.isEmpty() ? "OPEN" : latest.getFirst().getToStatus();
        if (revision != command.expectedRevision()) {
            throw new ServiceException("GROOVY_INCIDENT_STALE");
        }
        require("OPEN".equals(from) ? !"OPEN".equals(command.targetStatus()) : "OPEN".equals(command.targetStatus()));
        if ("RESOLVED".equals(command.targetStatus())) {
            require("SUCCEEDED".equals(row.getStatus()) || process.getInstanceStatus() != null
                    && Set.of("COMPLETED", "CANCELLED", "TERMINATED", "REJECTED").contains(process.getInstanceStatus()));
        }
        var action = new FmSystemTaskIncidentAction();
        action.setTenantId(tenantId);
        action.setExecutionOid(row.getOid());
        action.setActionNo(revision + 1);
        action.setRequestId(command.requestId());
        action.setFromStatus(from);
        action.setToStatus(command.targetStatus());
        action.setReason(command.reason().trim());
        action.setActor(actor);
        action.setActionDate(new Date());
        actions.insert(action).getValueEmptyThrowMessage();
        // Do not re-read with the caller's old repeatable-read snapshot after appending.
        return success(new FmGroovyIncidentHandlingView(action.getToStatus(), action.getActionNo(),
                List.of(actionView(action)), revision > 0));
    }

    private FmGroovyIncidentHandlingView current(String tenantId, String executionOid) throws ServiceException {
        var latest = actions.lockHistory(tenantId, executionOid, null);
        validateActions(tenantId, executionOid, latest);
        require(latest.size() == 1);
        var action = latest.getFirst();
        return new FmGroovyIncidentHandlingView(action.getToStatus(), action.getActionNo(),
                List.of(actionView(action)), action.getActionNo() > 1);
    }

    private FmSystemTaskExecution incident(String tenantId, String invocationId) throws ServiceException {
        require(id(invocationId));
        var rows = executions.findIncidents(tenantId, invocationId, 0, 2);
        require(rows != null && rows.size() == 1 && rows.getFirst() != null);
        var row = rows.getFirst();
        require(tenantId.equals(row.getTenantId()) && invocationId.equals(row.getInvocationId())
                && ("FAILED".equals(row.getStatus()) || row.getIncidentOid() != null || row.getParentInvocationId() != null)
                && row.getOid() != null && row.getProcessInstanceId() != null);
        return row;
    }

    private void authorize(String tenantId) throws ServiceException {
        access.require(tenantId);
        if (!enabled) {
            throw new ServiceException("GROOVY_INCIDENT_HANDLING_UNAVAILABLE");
        }
    }

    private void validateActions(String tenantId, String executionOid, List<FmSystemTaskIncidentAction> values)
            throws ServiceException {
        require(values != null);
        for (var value : values) {
            require(value != null && tenantId.equals(value.getTenantId()) && executionOid.equals(value.getExecutionOid())
                    && value.getActionNo() != null && value.getActionNo() > 0 && value.getToStatus() != null
                    && Set.of("OPEN", "RESOLVED", "IGNORED").contains(value.getToStatus()));
        }
    }

    private FmGroovyIncidentHandlingView view(List<FmSystemTaskIncidentAction> latest,
            List<FmSystemTaskIncidentAction> history, int offset) {
        return new FmGroovyIncidentHandlingView(latest.isEmpty() ? "OPEN" : latest.getFirst().getToStatus(),
                latest.isEmpty() ? 0 : latest.getFirst().getActionNo(),
                history.stream().limit(50).map(this::actionView).toList(), history.size() > 50 && offset < 1000000);
    }

    private FmGroovyIncidentHandlingView.Action actionView(FmSystemTaskIncidentAction value) {
        return new FmGroovyIncidentHandlingView.Action(value.getActionNo(), value.getFromStatus(), value.getToStatus(),
                value.getReason(), value.getActor(), value.getActionDate(), value.getActionType(), value.getTargetInvocationId());
    }

    private boolean manageable(FmSystemTaskExecution row) {
        return "FAILED".equals(row.getStatus()) || (row.getIncidentOid() != null || row.getParentInvocationId() != null)
                && Set.of("SUCCEEDED", "CANCELLED").contains(row.getStatus());
    }

    private boolean id(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}");
    }

    private void require(boolean valid) throws ServiceException {
        if (!valid) {
            throw new ServiceException("GROOVY_INCIDENT_ACTION_INVALID");
        }
    }

    private <T> DefaultResult<T> success(T value) {
        var result = new DefaultResult<T>();
        result.setSuccess(YesNoKeyProvide.YES);
        result.setValue(value);
        return result;
    }
}
