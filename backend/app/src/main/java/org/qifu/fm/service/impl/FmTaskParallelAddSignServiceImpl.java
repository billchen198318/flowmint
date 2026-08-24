package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.mapper.FmTaskParallelAddSignMapper;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskParallelAddSignServiceImpl
        extends BaseService<FmTaskParallelAddSign, String>
        implements IFmTaskParallelAddSignService {

    private final FmTaskParallelAddSignMapper mapper;

    public FmTaskParallelAddSignServiceImpl(FmTaskParallelAddSignMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmTaskParallelAddSign, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public FmTaskParallelAddSign findWaitingByParentTask(
            String tenantId, String parentTaskId) {
        return mapper.selectWaitingByParentTask(params(
                tenantId, "parentTaskId", parentTaskId));
    }

    @Override
    public FmTaskParallelAddSign findByRequestKey(
            String tenantId, String parentTaskId, String requestKey) {
        Map<String, Object> values = params(
                tenantId, "parentTaskId", parentTaskId);
        values.put("requestKey", requestKey);
        return mapper.selectByRequestKey(values);
    }

    @Override
    public FmTaskParallelAddSign findLatestByParentTask(
            String tenantId, String parentTaskId) {
        return mapper.selectLatestByParentTask(params(
                tenantId, "parentTaskId", parentTaskId));
    }

    @Override
    public List<FmTaskParallelAddSign> findByProcessInstance(
            String tenantId, String processInstanceId) {
        return mapper.selectByProcessInstance(params(
                tenantId, "processInstanceId", processInstanceId));
    }

    @Override
    public Integer nextBatchNo(String tenantId, String parentTaskId) {
        return mapper.selectNextBatchNo(params(
                tenantId, "parentTaskId", parentTaskId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int incrementResult(
            String tenantId, String oid, int lockVersion, boolean agreed) {
        Map<String, Object> values = params(tenantId, "oid", oid);
        values.put("lockVersion", lockVersion);
        values.put("agreed", agreed);
        values.put("updatedDate", new Date());
        return mapper.incrementResult(values);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int completeWaiting(String tenantId, String oid, int lockVersion) {
        Map<String, Object> values = params(tenantId, "oid", oid);
        values.put("lockVersion", lockVersion);
        values.put("completedDate", new Date());
        return mapper.completeWaiting(values);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int cancelWaiting(String tenantId, String oid, int lockVersion) {
        Map<String, Object> values = params(tenantId, "oid", oid);
        values.put("lockVersion", lockVersion);
        values.put("cancelledDate", new Date());
        return mapper.cancelWaiting(values);
    }

    private Map<String, Object> params(
            String tenantId, String name, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put(name, value);
        return params;
    }
}
