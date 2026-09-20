package org.qifu.fm.logic.impl;

import java.util.ArrayList;
import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.dto.view.FmGroovyIncidentPageView;
import org.qifu.fm.dto.view.FmGroovyIncidentView;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyIncidentLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmGroovyIncidentLogicServiceImpl implements IFmGroovyIncidentLogicService {

    private final FmGroovyOperateAccess access;
    private final IFmSystemTaskExecutionService executions;
    private final boolean enabled;

    public FmGroovyIncidentLogicServiceImpl(FmGroovyOperateAccess access, IFmSystemTaskExecutionService executions,
            @Value("${flowmint.groovy.runtime.enabled:false}") boolean runtimeEnabled,
            @Value("${flowmint.groovy.recovery.enabled:false}") boolean recoveryEnabled) {
        this.access = access;
        this.executions = executions;
        enabled = runtimeEnabled || recoveryEnabled;
    }

    @Override
    public DefaultResult<FmGroovyIncidentPageView> find(String tenantId, int offset) throws ServiceException {
        requireAccess(tenantId);
        if (offset < 0 || offset > 10000 || offset % 50 != 0) {
            throw new ServiceException("GROOVY_INCIDENT_QUERY_INVALID");
        }
        var rows = executions.findIncidents(tenantId, null, offset, 51);
        if (rows == null || rows.size() > 51) {
            throw new ServiceException("GROOVY_INCIDENT_QUERY_INVALID");
        }
        var views = new ArrayList<FmGroovyIncidentView>();
        for (var row : rows) {
            var view = view(tenantId, row);
            if (views.size() < 50) {
                views.add(view);
            }
        }
        return success(new FmGroovyIncidentPageView(List.copyOf(views), rows.size() > 50 && offset < 10000));
    }

    @Override
    public DefaultResult<FmGroovyIncidentView> detail(String tenantId, String invocationId) throws ServiceException {
        requireAccess(tenantId);
        if (invocationId == null || !invocationId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new ServiceException("GROOVY_INCIDENT_QUERY_INVALID");
        }
        var rows = executions.findIncidents(tenantId, invocationId, 0, 2);
        if (rows == null || rows.size() != 1 || rows.getFirst() == null
                || !invocationId.equals(rows.getFirst().getInvocationId())) {
            throw new ServiceException("GROOVY_INCIDENT_NOT_FOUND");
        }
        return success(view(tenantId, rows.getFirst()));
    }

    private void requireAccess(String tenantId) throws ServiceException {
        access.require(tenantId);
        if (!enabled) {
            throw new ServiceException("GROOVY_OPERATIONS_UNAVAILABLE");
        }
    }

    private FmGroovyIncidentView view(String tenantId, FmSystemTaskExecution row) throws ServiceException {
        if (row == null || !tenantId.equals(row.getTenantId())
                || !"FAILED".equals(row.getStatus()) && row.getIncidentOid() == null && row.getParentInvocationId() == null) {
            throw new ServiceException("GROOVY_INCIDENT_QUERY_INVALID");
        }
        String category = switch (row.getErrorCode() == null ? "" : row.getErrorCode()) {
            case "GROOVY_LEASE_EXPIRED" -> "執行租約到期";
            case "GROOVY_START_FAILED" -> "工作尚未開始即進入失敗佇列";
            case "GROOVY_JOB_MISSING" -> "找不到待執行工作，需檢查排程";
            case "GROOVY_EXECUTION_REMOVED" -> "引擎執行已移除或離開原節點";
            case "GROOVY_PROCESS_ENDED_BEFORE_START" -> "流程已結束，工作未執行";
            case "GROOVY_TIMEOUT" -> "腳本執行逾時";
            case "GROOVY_MEMORY_LIMIT" -> "超過記憶體限制";
            case "GROOVY_COMPILE_ERROR", "GROOVY_POLICY_DENIED" -> "腳本編譯或政策檢查失敗";
            case "GROOVY_INPUT_INVALID", "GROOVY_OUTPUT_INVALID", "GROOVY_RUNTIME_CONTRACT_INVALID" -> "輸入或輸出契約不符";
            case "ENGINE_PROFILE_DISABLED" -> "執行環境已撤銷";
            case "GROOVY_PROFILE_POLICY_UNAVAILABLE" -> "無法確認執行環境撤銷政策";
            case "GROOVY_RUNTIME_UNAVAILABLE", "GROOVY_RUNTIME_RUNNER_FAILED",
                    "GROOVY_RUNTIME_START_UNAVAILABLE" -> "執行服務不可用";
            case "GROOVY_RUNTIME_BUSY" -> "執行服務忙碌";
            default -> "執行失敗，需由管理員檢查";
        };
        if (row.getParentInvocationId() != null && row.getErrorCode() == null) {
            category = "由原異常建立的重算工作";
        }
        return new FmGroovyIncidentView(row.getIncidentOid() == null ? row.getOid() : row.getIncidentOid(),
                row.getInvocationId(), row.getProcessInstanceId(), row.getProcessDefId(), row.getVersionNo(),
                row.getNodeId(), row.getAttemptNo(), category, row.getStartedAt(), row.getCompletedAt(), row.getStatus(),
                row.getParentInvocationId());
    }

    private <T> DefaultResult<T> success(T value) {
        var result = new DefaultResult<T>();
        result.setValue(value);
        result.setSuccess(YesNoKeyProvide.YES);
        return result;
    }
}
