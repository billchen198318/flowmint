package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmSystemTaskAttempt;
import org.qifu.fm.mapper.FmSystemTaskAttemptMapper;
import org.qifu.fm.service.IFmSystemTaskAttemptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmSystemTaskAttemptServiceImpl extends BaseService<FmSystemTaskAttempt, String>
        implements IFmSystemTaskAttemptService {

    private final FmSystemTaskAttemptMapper mapper;

    public FmSystemTaskAttemptServiceImpl(FmSystemTaskAttemptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmSystemTaskAttempt, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public String getAccountId() {
        return "SYSTEM_TASK";
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmSystemTaskAttempt> insert(FmSystemTaskAttempt value) throws ServiceException {
        if (value == null || !"RUNNING".equals(value.getStatus()) || value.getGeneration() == null
                || value.getGeneration() < 1 || value.getAttemptNo() == null || value.getAttemptNo() < 1
                || value.getStartedAt() == null || value.getLeaseUntil() == null
                || !value.getLeaseUntil().after(value.getStartedAt()) || value.getCompletedAt() != null
                || value.getErrorCode() != null || value.getDurationMs() != null) {
            throw new ServiceException("SYSTEM_TASK_INITIAL_STATE_INVALID");
        }
        return super.insert(value);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int finish(String tenantId, String executionOid, String requestId, int generation,
            String status, Date now, String errorCode) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || executionOid == null || executionOid.isBlank()
                || requestId == null || requestId.isBlank() || generation < 1 || now == null
                || !("SUCCEEDED".equals(status) || "FAILED".equals(status)
                        || "ABANDONED".equals(status) || "CANCELLED".equals(status))
                || "SUCCEEDED".equals(status) && errorCode != null
                || !"SUCCEEDED".equals(status) && (errorCode == null || !errorCode.matches("[A-Z][A-Z0-9_]{0,99}"))) {
            throw new ServiceException("SYSTEM_TASK_ATTEMPT_FINISH_INVALID");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("executionOid", executionOid);
        params.put("requestId", requestId);
        params.put("generation", generation);
        params.put("status", status);
        params.put("now", now);
        params.put("errorCode", errorCode);
        return mapper.finish(params);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public int cancelRunning(String tenantId, String executionOid, int generation, Date now) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || executionOid == null || executionOid.isBlank()
                || generation < 0 || now == null) {
            throw new ServiceException("SYSTEM_TASK_CANCEL_INVALID");
        }
        return mapper.cancelRunning(Map.of("tenantId", tenantId, "executionOid", executionOid,
                "generation", generation, "now", now));
    }

    @Override
    public DefaultResult<FmSystemTaskAttempt> update(FmSystemTaskAttempt value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_UPDATE_DENIED");
    }

    @Override
    public DefaultResult<Boolean> delete(FmSystemTaskAttempt value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_DELETE_DENIED");
    }
}
