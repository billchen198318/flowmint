package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmSystemTaskIncidentAction;
import org.qifu.fm.mapper.FmSystemTaskIncidentActionMapper;
import org.qifu.fm.service.IFmSystemTaskIncidentActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmSystemTaskIncidentActionServiceImpl extends BaseService<FmSystemTaskIncidentAction, String>
        implements IFmSystemTaskIncidentActionService {

    private final FmSystemTaskIncidentActionMapper mapper;

    public FmSystemTaskIncidentActionServiceImpl(FmSystemTaskIncidentActionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmSystemTaskIncidentAction, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public String getAccountId() {
        String accountId = super.getAccountId();
        return accountId == null || accountId.isBlank() ? "SYSTEM_TASK" : accountId;
    }

    @Override
    public List<FmSystemTaskIncidentAction> history(String tenantId, String executionOid, int offset, int limit)
            throws ServiceException {
        return mapper.history(parameters(tenantId, executionOid, offset, limit));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public List<FmSystemTaskIncidentAction> lockHistory(String tenantId, String executionOid, String requestId)
            throws ServiceException {
        var params = parameters(tenantId, executionOid, 0, 1);
        params.put("requestId", requestId);
        params.put("locking", true);
        return mapper.history(params);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmSystemTaskIncidentAction> insert(FmSystemTaskIncidentAction value) throws ServiceException {
        var states = Set.of("OPEN", "RESOLVED", "IGNORED");
        if (value == null || value.getActionNo() == null || value.getActionNo() < 1
                || value.getFromStatus() == null || !states.contains(value.getFromStatus())
                || value.getToStatus() == null || !states.contains(value.getToStatus())
                || !validAction(value)
                || value.getReason() == null || value.getReason().isBlank() || value.getReason().length() > 1000
                || value.getActor() == null || value.getActor().isBlank() || value.getActor().length() > 24
                || value.getRequestId() == null || !value.getRequestId().matches("[A-Za-z0-9_-]{1,64}")
                || value.getActionDate() == null) {
            throw new ServiceException("GROOVY_INCIDENT_ACTION_INVALID");
        }
        parameters(value.getTenantId(), value.getExecutionOid(), 0, 1);
        return super.insert(value);
    }

    @Override
    public DefaultResult<FmSystemTaskIncidentAction> update(FmSystemTaskIncidentAction value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_UPDATE_DENIED");
    }

    @Override
    public DefaultResult<Boolean> delete(FmSystemTaskIncidentAction value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_DELETE_DENIED");
    }

    private boolean validAction(FmSystemTaskIncidentAction value) {
        if ("RECALCULATE".equals(value.getActionType())) {
            return "OPEN".equals(value.getFromStatus()) && "IGNORED".equals(value.getToStatus())
                    && value.getExpectedGeneration() != null && value.getExpectedGeneration() > 0
                    && value.getTargetInvocationId() != null && value.getTargetInvocationId().matches("[A-Za-z0-9_-]{1,36}")
                    && value.getRequestSha256() != null && value.getRequestSha256().matches("[a-f0-9]{64}");
        }
        if (value.getTargetInvocationId() != null || value.getRequestSha256() != null) return false;
        return "STATUS".equals(value.getActionType())
                ? !value.getFromStatus().equals(value.getToStatus()) && value.getExpectedGeneration() == null
                : "RETRY".equals(value.getActionType()) && "OPEN".equals(value.getFromStatus())
                        && "OPEN".equals(value.getToStatus()) && value.getExpectedGeneration() != null
                        && value.getExpectedGeneration() > 0;
    }

    private Map<String, Object> parameters(String tenantId, String executionOid, int offset, int limit)
            throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || executionOid == null || executionOid.isBlank()
                || offset < 0 || offset > 1000000 || limit < 1 || limit > 51) {
            throw new ServiceException("GROOVY_INCIDENT_HISTORY_INVALID");
        }
        return new HashMap<>(Map.of("tenantId", tenantId, "executionOid", executionOid,
                "offset", offset, "limit", limit));
    }
}
