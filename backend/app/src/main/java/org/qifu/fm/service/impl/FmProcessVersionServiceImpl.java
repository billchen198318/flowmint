package org.qifu.fm.service.impl;

import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.mapper.FmProcessVersionMapper;
import org.qifu.fm.service.IFmProcessVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmProcessVersionServiceImpl extends BaseService<FmProcessVersion, String>
        implements IFmProcessVersionService {

    private final FmProcessVersionMapper mapper;

    public FmProcessVersionServiceImpl(FmProcessVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessVersion, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public Integer findLockVersion(String tenantId, String oid) {
        return mapper.findLockVersion(Map.of("tenantId", tenantId, "oid", oid));
    }

    @Override
    public Integer findDraftLockVersion(String tenantId, String oid) {
        return mapper.findDraftLockVersion(Map.of("tenantId", tenantId, "oid", oid));
    }

    @Override
    @Transactional(readOnly = false)
    public Integer lockDraft(String tenantId, String oid) {
        return mapper.lockDraft(Map.of("tenantId", tenantId, "oid", oid));
    }

    @Override
    @Transactional(readOnly = false)
    public int advanceDraftLock(String tenantId, String oid, Integer expectedLockVersion) {
        return mapper.advanceDraftLock(Map.of("tenantId", tenantId, "oid", oid,
                "expectedLockVersion", expectedLockVersion));
    }
}
