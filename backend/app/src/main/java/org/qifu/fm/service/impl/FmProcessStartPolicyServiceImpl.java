package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessStartPolicy;
import org.qifu.fm.mapper.FmProcessStartPolicyMapper;
import org.qifu.fm.service.IFmProcessStartPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessStartPolicyServiceImpl
        extends BaseService<FmProcessStartPolicy, String>
        implements IFmProcessStartPolicyService {

    private final FmProcessStartPolicyMapper mapper;

    public FmProcessStartPolicyServiceImpl(FmProcessStartPolicyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessStartPolicy, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmProcessStartPolicy> findByVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo) {
        return mapper.selectByVersion(parameters(
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
            List<FmProcessStartPolicy> policies) {
        mapper.deleteByVersion(parameters(tenantId, processDefId, processVersionNo));
        policies.forEach(this::insert);
    }

    private Map<String, Object> parameters(
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
