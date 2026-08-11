package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.mapper.FmTaskAssignmentRuleMapper;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmTaskAssignmentRuleServiceImpl extends BaseService<FmTaskAssignmentRule, String>
        implements IFmTaskAssignmentRuleService {

    private final FmTaskAssignmentRuleMapper mapper;

    public FmTaskAssignmentRuleServiceImpl(FmTaskAssignmentRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmTaskAssignmentRule, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmTaskAssignmentRule> findByVersion(
            String tenantId,
            String processDefId,
            Integer versionNo) {
        return mapper.selectByVersion(versionParameters(
                tenantId,
                processDefId,
                versionNo));
    }

    @Override
    @Transactional(readOnly = false)
    public void replaceVersion(
            String tenantId,
            String processDefId,
            Integer versionNo,
            List<FmTaskAssignmentRule> rules) {
        mapper.deleteByVersion(versionParameters(
                tenantId,
                processDefId,
                versionNo));
        rules.forEach(this::insert);
    }

    private Map<String, Object> versionParameters(
            String tenantId,
            String processDefId,
            Integer versionNo) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("processDefId", processDefId);
        parameters.put("processVersionNo", versionNo);
        return parameters;
    }
}
