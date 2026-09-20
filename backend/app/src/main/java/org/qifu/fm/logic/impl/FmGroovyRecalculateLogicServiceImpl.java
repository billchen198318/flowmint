package org.qifu.fm.logic.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.workflow.FmGroovyContractJson;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.domain.workflow.FmGroovyRetryJob;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeRunner;
import org.qifu.fm.dto.command.FmGroovyRecalculateCommand;
import org.qifu.fm.dto.view.FmGroovyRecalculateView;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.logic.IFmGroovyRecalculateLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService;
import org.qifu.fm.logic.IFmGroovyRuntimeInputLogicService.LoadedInput;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

@Service
@Transactional(transactionManager = "transactionManager", readOnly = true, rollbackFor = Exception.class)
public class FmGroovyRecalculateLogicServiceImpl implements IFmGroovyRecalculateLogicService {
    private final FmGroovyOperateAccess access;
    private final IFmSystemTaskExecutionService executions;
    private final IFmSystemTaskIncidentActionService actions;
    private final IFmProcessInstanceService processes;
    private final IFmFormDataService forms;
    private final IFmGroovyRuntimeInputLogicService inputs;
    private final FmGroovyRuntimeRunner runner;
    private final FmGroovyRetryJob jobs;
    private final boolean enabled;

    public FmGroovyRecalculateLogicServiceImpl(FmGroovyOperateAccess access, IFmSystemTaskExecutionService executions,
            IFmSystemTaskIncidentActionService actions, IFmProcessInstanceService processes, IFmFormDataService forms,
            IFmGroovyRuntimeInputLogicService inputs, FmGroovyRuntimeRunner runner, FmGroovyRetryJob jobs,
            @Value("${flowmint.groovy.recalculate.enabled:false}") boolean enabled,
            @Value("${flowmint.groovy.incident-handling.enabled:false}") boolean handlingEnabled) {
        this.access = access;
        this.executions = executions;
        this.actions = actions;
        this.processes = processes;
        this.forms = forms;
        this.inputs = inputs;
        this.runner = runner;
        this.jobs = jobs;
        this.enabled = enabled && handlingEnabled;
    }

    @Override
    public DefaultResult<FmGroovyRecalculateView> preview(String tenantId, String invocationId) throws ServiceException {
        authorize(tenantId);
        var row = observed(tenantId, invocationId);
        var latest = actions.history(tenantId, row.getOid(), 0, 1);
        int revision = revision(tenantId, row.getOid(), latest);
        eligible(row, latest);
        var loaded = previewInput(row);
        var processRows = processes.selectListByParams(Map.of("tenantId", tenantId,
                "processInstanceId", row.getProcessInstanceId())).getValue();
        require(processRows != null && processRows.size() == 1 && processRows.getFirst() != null);
        var process = processRows.getFirst();
        require(tenantId.equals(process.getTenantId()) && row.getProcessInstanceId().equals(process.getProcessInstanceId())
                && "RUNNING".equals(process.getInstanceStatus()));
        jobs.check(row, process.getFlowableProcessDefId());
        return success(new FmGroovyRecalculateView(invocationId, revision, row.getGeneration(), row.getInputRevisionNo(),
                loaded.snapshot().revisionNo(), loaded.expectedFormLock(), loaded.snapshot().sha256(),
                loaded.preparation().bindingSha256()));
    }

    @Override
    @Transactional(transactionManager = "transactionManager", readOnly = false, rollbackFor = Exception.class, timeout = 30)
    public DefaultResult<String> recalculate(String tenantId, FmGroovyRecalculateCommand command) throws ServiceException {
        authorize(tenantId);
        require(command != null && id(command.requestId()) && command.expectedRevision() >= 0
                && command.expectedRevision() < Integer.MAX_VALUE && command.expectedGeneration() > 0
                && command.formRevision() > 0 && command.formLock() >= 0 && hash(command.inputSha256())
                && hash(command.bindingSha256()) && command.reason() != null && !command.reason().isBlank()
                && command.reason().trim().length() <= 1000);
        var user = UserUtils.getCurrentUser();
        require(user != null && user.getUserId() != null && !user.getUserId().isBlank() && user.getUserId().length() <= 24);
        String actor = user.getUserId();
        var observed = observed(tenantId, command.invocationId());
        var process = processes.lockInstance(tenantId, observed.getProcessInstanceId());
        require(process != null && tenantId.equals(process.getTenantId())
                && observed.getProcessInstanceId().equals(process.getProcessInstanceId()));
        var row = executions.lockInvocation(tenantId, command.invocationId());
        require(row != null && tenantId.equals(row.getTenantId()) && observed.getOid().equals(row.getOid())
                && observed.getProcessInstanceId().equals(row.getProcessInstanceId())
                && command.invocationId().equals(row.getInvocationId()) && "GROOVY".equals(row.getTaskType()));
        String fingerprint = fingerprint(tenantId, command);
        var previous = actions.lockHistory(tenantId, row.getOid(), command.requestId());
        revision(tenantId, row.getOid(), previous);
        if (!previous.isEmpty()) {
            var action = previous.getFirst();
            require(command.requestId().equals(action.getRequestId()) && "RECALCULATE".equals(action.getActionType())
                    && actor.equals(action.getActor()) && command.reason().trim().equals(action.getReason())
                    && fingerprint.equals(action.getRequestSha256()) && id(action.getTargetInvocationId()));
            return success(action.getTargetInvocationId());
        }
        var latest = actions.lockHistory(tenantId, row.getOid(), null);
        require(revision(tenantId, row.getOid(), latest) == command.expectedRevision()
                && Integer.valueOf(command.expectedGeneration()).equals(row.getGeneration())
                && "RUNNING".equals(process.getInstanceStatus()));
        eligible(row, latest);
        // Current form lock prevents another revision from being accepted between preview validation and insert.
        var form = forms.lockStateByFormDataId(tenantId, row.getFormDataId());
        require(form != null && tenantId.equals(form.getTenantId()) && row.getFormDataId().equals(form.getFormDataId())
                && Integer.valueOf(command.formRevision()).equals(form.getRevisionNo())
                && Integer.valueOf(command.formLock()).equals(form.getLockVersion()) && "SUBMITTED".equals(form.getDataStatus()));
        var preview = previewInput(row);
        require(command.formRevision() == preview.snapshot().revisionNo() && command.formLock() == preview.expectedFormLock()
                && command.inputSha256().equals(preview.snapshot().sha256())
                && command.bindingSha256().equals(preview.preparation().bindingSha256())
                && Objects.equals(form.getDataContent(), preview.savedFormJson()));
        String correlation = UUID.randomUUID().toString();
        String invocation = FmGroovyJobIdentity.invocationForCorrelation(tenantId, correlation);
        Instant started = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var loaded = inputs.load(tenantId, row.getFlowableExecutionId(), invocation, started, runner.profile());
        sameDefinition(row, loaded);
        require(preview.snapshot().revisionNo() == loaded.snapshot().revisionNo()
                && preview.expectedFormLock() == loaded.expectedFormLock()
                && preview.savedFormJson().equals(loaded.savedFormJson()));
        String jobId = jobs.recalculate(row, process.getFlowableProcessDefId(), correlation);
        require(id(jobId));
        var successor = initial(row, loaded, invocation, jobId, Date.from(started));
        executions.insert(successor).getValueEmptyThrowMessage();
        var action = new FmSystemTaskIncidentAction();
        action.setTenantId(tenantId);
        action.setExecutionOid(row.getOid());
        action.setActionNo(command.expectedRevision() + 1);
        action.setRequestId(command.requestId());
        action.setActionType("RECALCULATE");
        action.setExpectedGeneration(row.getGeneration());
        action.setRequestSha256(fingerprint);
        action.setTargetInvocationId(invocation);
        action.setFromStatus("OPEN");
        action.setToStatus("IGNORED");
        action.setReason(command.reason().trim());
        action.setActor(actor);
        action.setActionDate(new Date());
        actions.insert(action).getValueEmptyThrowMessage();
        return success(invocation);
    }

    private LoadedInput previewInput(FmSystemTaskExecution row) throws ServiceException {
        var loaded = inputs.load(row.getTenantId(), row.getFlowableExecutionId(), row.getInvocationId(),
                row.getStartedAt().toInstant(), runner.profile());
        sameDefinition(row, loaded);
        // A new invocation is not a way to replenish a spent retry budget for an unchanged form.
        require(loaded.snapshot().revisionNo() > row.getInputRevisionNo());
        return loaded;
    }

    private void sameDefinition(FmSystemTaskExecution row, LoadedInput loaded) throws ServiceException {
        require(loaded != null && Objects.equals(row.getTenantId(), loaded.tenantId())
                && Objects.equals(row.getFlowableExecutionId(), loaded.executionId())
                && Objects.equals(row.getProcessInstanceId(), loaded.processInstanceId())
                && Objects.equals(row.getProcessDefId(), loaded.processDefId())
                && Objects.equals(row.getVersionNo(), loaded.processVersionNo()) && Objects.equals(row.getNodeId(), loaded.nodeId())
                && Objects.equals(row.getFormDataId(), loaded.formDataId())
                && Objects.equals(row.getBindingSha256(), loaded.preparation().bindingSha256())
                && Objects.equals(row.getManifestSha256(), loaded.preparation().manifestSha256()));
    }

    private FmSystemTaskExecution initial(FmSystemTaskExecution parent, LoadedInput loaded,
            String invocation, String jobId, Date started) {
        var row = new FmSystemTaskExecution();
        row.setTenantId(parent.getTenantId());
        row.setInvocationId(invocation);
        row.setParentInvocationId(parent.getInvocationId());
        row.setProcessInstanceId(loaded.processInstanceId());
        row.setProcessDefId(loaded.processDefId());
        row.setVersionNo(loaded.processVersionNo());
        row.setNodeId(loaded.nodeId());
        row.setFlowableExecutionId(loaded.executionId());
        row.setFirstJobId(jobId);
        row.setTaskType("GROOVY");
        row.setBindingSha256(loaded.preparation().bindingSha256());
        row.setManifestSha256(loaded.preparation().manifestSha256());
        row.setFormDataId(loaded.formDataId());
        row.setInputRevisionNo(loaded.snapshot().revisionNo());
        row.setExpectedFormLock(loaded.expectedFormLock());
        row.setInputContent(loaded.snapshot().inputJson());
        row.setContextContent(loaded.snapshot().contextJson());
        row.setInputSha256(loaded.snapshot().sha256());
        row.setStartedAt(started);
        row.setStatus("READY");
        row.setGeneration(0);
        row.setAttemptNo(0);
        return row;
    }

    private void eligible(FmSystemTaskExecution row, List<FmSystemTaskIncidentAction> latest) throws ServiceException {
        require("FAILED".equals(row.getStatus()) && row.getGeneration() != null && row.getGeneration() > 0
                && row.getAttemptNo() != null && row.getAttemptNo() >= 0
                && row.getInputRevisionNo() != null && row.getInputRevisionNo() > 0 && row.getStartedAt() != null
                && row.getLeaseUntil() == null && row.getCompletedAt() != null && row.getResultSha256() == null
                && row.getFormSnapshotOid() == null && (latest.isEmpty() || "OPEN".equals(latest.getFirst().getToStatus())));
    }

    private FmSystemTaskExecution observed(String tenantId, String invocation) throws ServiceException {
        require(id(invocation));
        var rows = executions.selectListByParams(Map.of("tenantId", tenantId, "invocationId", invocation)).getValue();
        require(rows != null && rows.size() == 1 && rows.getFirst() != null);
        var row = rows.getFirst();
        require(tenantId.equals(row.getTenantId()) && invocation.equals(row.getInvocationId())
                && "GROOVY".equals(row.getTaskType()) && row.getOid() != null && row.getProcessInstanceId() != null);
        return row;
    }

    private int revision(String tenantId, String executionOid, List<FmSystemTaskIncidentAction> values) throws ServiceException {
        require(values != null && values.size() <= 1);
        if (values.isEmpty()) return 0;
        var action = values.getFirst();
        require(action != null && tenantId.equals(action.getTenantId()) && executionOid.equals(action.getExecutionOid())
                && action.getActionNo() != null && action.getActionNo() > 0);
        return action.getActionNo();
    }

    private String fingerprint(String tenantId, FmGroovyRecalculateCommand command) {
        return FmGroovyContractJson.sha256(JsonMapper.builder().build().writeValueAsString(List.of(
                "recalculate-v1", tenantId, command.invocationId(), command.expectedRevision(), command.expectedGeneration(),
                command.formRevision(), command.formLock(), command.inputSha256(), command.bindingSha256())));
    }

    private void authorize(String tenantId) throws ServiceException {
        access.require(tenantId);
        if (!enabled) throw new ServiceException("GROOVY_RECALCULATE_UNAVAILABLE");
    }

    private boolean id(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,64}");
    }

    private boolean hash(String value) {
        return value != null && value.matches("[a-f0-9]{64}");
    }

    private static void require(boolean valid) throws ServiceException {
        if (!valid) throw new ServiceException("GROOVY_RECALCULATE_INVALID");
    }

    private <T> DefaultResult<T> success(T value) {
        var result = new DefaultResult<T>();
        result.setValue(value);
        result.setSuccess(YesNoKeyProvide.YES);
        return result;
    }
}
