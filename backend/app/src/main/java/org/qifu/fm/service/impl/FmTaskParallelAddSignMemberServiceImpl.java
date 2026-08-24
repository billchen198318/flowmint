package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskParallelAddSignMember;
import org.qifu.fm.mapper.FmTaskParallelAddSignMemberMapper;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskParallelAddSignMemberServiceImpl
        extends BaseService<FmTaskParallelAddSignMember, String>
        implements IFmTaskParallelAddSignMemberService {

    private final FmTaskParallelAddSignMemberMapper mapper;

    public FmTaskParallelAddSignMemberServiceImpl(
            FmTaskParallelAddSignMemberMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmTaskParallelAddSignMember, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmTaskParallelAddSignMember> findByBatch(
            String tenantId, String parallelAddSignOid) {
        return mapper.selectByBatch(params(
                tenantId, "parallelAddSignOid", parallelAddSignOid));
    }

    @Override
    public FmTaskParallelAddSignMember findPendingByTask(
            String tenantId, String flowableTaskId) {
        return mapper.selectPendingByTask(params(
                tenantId, "flowableTaskId", flowableTaskId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int completePending(
            String tenantId, String oid, int lockVersion,
            String status, String comment) {
        Map<String, Object> values = params(tenantId, "oid", oid);
        values.put("lockVersion", lockVersion);
        values.put("status", status);
        values.put("comment", comment);
        values.put("completedDate", new Date());
        return mapper.completePending(values);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int cancelPendingByBatch(
            String tenantId, String parallelAddSignOid) {
        return mapper.cancelPendingByBatch(params(
                tenantId, "parallelAddSignOid", parallelAddSignOid));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    public int reassignPending(
            String tenantId, String oid, int lockVersion, String targetAccount,
            String actor) {
        Map<String, Object> values = params(tenantId, "oid", oid);
        values.put("lockVersion", lockVersion);
        values.put("targetAccount", targetAccount);
        values.put("actor", actor);
        return mapper.reassignPending(values);
    }

    private Map<String, Object> params(
            String tenantId, String name, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put("tenantId", tenantId);
        params.put(name, value);
        return params;
    }
}
