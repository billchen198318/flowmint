package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.mapper.FmSystemTaskExecutionMapper;
import org.qifu.fm.service.IFmSystemTaskExecutionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmSystemTaskExecutionServiceImpl extends BaseService<FmSystemTaskExecution, String>
        implements IFmSystemTaskExecutionService {

    private final FmSystemTaskExecutionMapper mapper;

    public FmSystemTaskExecutionServiceImpl(FmSystemTaskExecutionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmSystemTaskExecution, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public String getAccountId() {
        return "SYSTEM_TASK";
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public FmSystemTaskExecution lockInvocation(String tenantId, String invocationId) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || invocationId == null || invocationId.isBlank()) {
            throw new ServiceException("SYSTEM_TASK_IDENTITY_REQUIRED");
        }
        return mapper.lockInvocation(Map.of("tenantId", tenantId, "invocationId", invocationId));
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int fail(String tenantId, String oid, int generation, Date now, String errorCode) throws ServiceException {
        Map<String, Object> params = identity(tenantId, oid, now);
        if (generation < 1 || errorCode == null || !errorCode.matches("[A-Z][A-Z0-9_]{0,99}")) {
            throw new ServiceException("SYSTEM_TASK_FAILURE_INVALID");
        }
        params.put("generation", generation);
        params.put("errorCode", errorCode);
        return mapper.fail(params);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public DefaultResult<FmSystemTaskExecution> insert(FmSystemTaskExecution value) throws ServiceException {
        if (value == null || !"READY".equals(value.getStatus()) || !Integer.valueOf(0).equals(value.getGeneration())
                || !Integer.valueOf(0).equals(value.getAttemptNo()) || value.getLeaseUntil() != null
                || value.getCompletedAt() != null || value.getResultSha256() != null) {
            throw new ServiceException("SYSTEM_TASK_INITIAL_STATE_INVALID");
        }
        return super.insert(value);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int claim(String tenantId, String oid, int generation, Date now, Date leaseUntil) throws ServiceException {
        Map<String, Object> params = identity(tenantId, oid, now);
        if (generation < 0 || generation == Integer.MAX_VALUE || leaseUntil == null || !leaseUntil.after(now)
                || leaseUntil.getTime() - now.getTime() > 60000) {
            throw new ServiceException("SYSTEM_TASK_LEASE_INVALID");
        }
        params.put("generation", generation);
        params.put("leaseUntil", leaseUntil);
        return mapper.claim(params);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int complete(String tenantId, String oid, int generation, Date now,
            String resultSha256, String formSnapshotOid) throws ServiceException {
        Map<String, Object> params = identity(tenantId, oid, now);
        if (generation < 1 || resultSha256 == null || !resultSha256.matches("[a-f0-9]{64}")) {
            throw new ServiceException("SYSTEM_TASK_RECEIPT_INVALID");
        }
        params.put("generation", generation);
        params.put("resultSha256", resultSha256);
        params.put("formSnapshotOid", formSnapshotOid);
        return mapper.complete(params);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int cancel(String tenantId, String oid, Date now) throws ServiceException {
        return mapper.cancel(identity(tenantId, oid, now));
    }

    @Override
    public List<FmSystemTaskExecution> findExpired(String tenantId, Date now, int limit) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || now == null || limit < 1 || limit > 100) {
            throw new ServiceException("SYSTEM_TASK_EXPIRY_QUERY_INVALID");
        }
        return mapper.findExpired(Map.of("tenantId", tenantId, "now", now, "limit", limit));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public int expire(String tenantId, String oid, int generation, Date now) throws ServiceException {
        Map<String, Object> params = identity(tenantId, oid, now);
        if (generation < 1) {
            throw new ServiceException("SYSTEM_TASK_GENERATION_INVALID");
        }
        params.put("generation", generation);
        return mapper.expire(params);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false)
    public List<FmSystemTaskExecution> lockActiveForProcess(String tenantId, String processInstanceId) {
        if (tenantId == null || tenantId.isBlank() || processInstanceId == null || processInstanceId.isBlank()) {
            throw new IllegalArgumentException("SYSTEM_TASK_IDENTITY_REQUIRED");
        }
        return mapper.lockActiveForProcess(Map.of("tenantId", tenantId, "processInstanceId", processInstanceId));
    }

    @Override
    public List<FmSystemTaskExecution> findIncidents(String tenantId, String invocationId, int offset, int limit)
            throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || offset < 0 || offset > 10000 || limit < 1 || limit > 51
                || invocationId != null && !invocationId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new ServiceException("SYSTEM_TASK_INCIDENT_QUERY_INVALID");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("invocationId", invocationId);
        params.put("offset", offset);
        params.put("limit", limit);
        return mapper.findIncidents(params);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public int rearm(String tenantId, String oid, int generation) throws ServiceException {
        var params = identity(tenantId, oid, new Date());
        if (generation < 1 || generation >= Integer.MAX_VALUE - 1) {
            throw new ServiceException("SYSTEM_TASK_GENERATION_INVALID");
        }
        params.put("generation", generation);
        return mapper.rearm(params);
    }

    @Override
    public List<FmSystemTaskExecution> findStalledReady(String tenantId, Date cutoff, String afterOid, int limit) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || cutoff == null || limit < 1 || limit > 100
                || afterOid != null && !afterOid.matches("[A-Za-z0-9_-]{1,36}")) {
            throw new ServiceException("SYSTEM_TASK_READY_QUERY_INVALID");
        }
        var params = new HashMap<String, Object>();
        params.put("tenantId", tenantId);
        params.put("cutoff", cutoff);
        params.put("afterOid", afterOid);
        params.put("limit", limit);
        return mapper.findStalledReady(params);
    }

    @Override
    public List<FmSystemTaskExecution> findAutomaticRetries(String tenantId, Date now, String afterOid, int limit)
            throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || now == null || limit < 1 || limit > 100
                || afterOid != null && !afterOid.matches("[A-Za-z0-9_-]{1,36}")) {
            throw new ServiceException("SYSTEM_TASK_RETRY_QUERY_INVALID");
        }
        var params = new HashMap<String, Object>();
        params.put("tenantId", tenantId);
        params.put("firstCutoff", Date.from(now.toInstant().minusSeconds(1)));
        params.put("secondCutoff", Date.from(now.toInstant().minusSeconds(5)));
        params.put("afterOid", afterOid);
        params.put("limit", limit);
        return mapper.findAutomaticRetries(params);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, readOnly = false, rollbackFor = Exception.class)
    public int finishReady(String tenantId, String oid, int generation, Date cutoff, Date now, String status, String errorCode)
            throws ServiceException {
        var params = identity(tenantId, oid, now);
        boolean allowed = "FAILED".equals(status)
                ? "GROOVY_START_FAILED".equals(errorCode) || "GROOVY_JOB_MISSING".equals(errorCode)
                : "CANCELLED".equals(status)
                        && ("GROOVY_EXECUTION_REMOVED".equals(errorCode) || "GROOVY_PROCESS_ENDED_BEFORE_START".equals(errorCode));
        if (generation < 0 || generation == Integer.MAX_VALUE || cutoff == null || cutoff.after(now) || !allowed) {
            throw new ServiceException("SYSTEM_TASK_READY_RECOVERY_INVALID");
        }
        params.put("generation", generation);
        params.put("cutoff", cutoff);
        params.put("status", status);
        params.put("errorCode", errorCode);
        return mapper.finishReady(params);
    }

    private Map<String, Object> identity(String tenantId, String oid, Date now) throws ServiceException {
        if (tenantId == null || tenantId.isBlank() || oid == null || oid.isBlank() || now == null) {
            throw new ServiceException("SYSTEM_TASK_IDENTITY_REQUIRED");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put("oid", oid);
        params.put("now", now);
        return params;
    }

    @Override
    public DefaultResult<FmSystemTaskExecution> update(FmSystemTaskExecution value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_UPDATE_DENIED");
    }

    @Override
    public DefaultResult<Boolean> delete(FmSystemTaskExecution value) throws ServiceException {
        throw new ServiceException("SYSTEM_TASK_LEDGER_DELETE_DENIED");
    }
}
