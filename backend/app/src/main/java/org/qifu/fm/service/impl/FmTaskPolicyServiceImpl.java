package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.mapper.FmTaskPolicyMapper;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskPolicyServiceImpl extends BaseService<FmTaskPolicy, String>
        implements IFmTaskPolicyService {

    private final FmTaskPolicyMapper mapper;

    public FmTaskPolicyServiceImpl(FmTaskPolicyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmTaskPolicy, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmTaskPolicy> findByVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo) {
        return mapper.selectByVersion(versionParameters(
                tenantId,
                processDefId,
                processVersionNo));
    }

    @Override
    @Transactional(readOnly = false)
    public void replaceVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo,
            List<FmTaskPolicy> policies) {
        mapper.deleteByVersion(versionParameters(
                tenantId,
                processDefId,
                processVersionNo));
        policies.forEach(this::insert);
    }

    private Map<String, Object> versionParameters(
            String tenantId,
            String processDefId,
            Integer processVersionNo) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        parameters.put("processVersionNo", processVersionNo);
        return parameters;
    }
}
