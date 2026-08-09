package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApprovalAuthorityRule;
import org.qifu.fm.mapper.FmApprovalAuthorityRuleMapper;
import org.qifu.fm.service.IFmApprovalAuthorityRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmApprovalAuthorityRuleServiceImpl extends BaseService<FmApprovalAuthorityRule, String>
        implements IFmApprovalAuthorityRuleService {

    private final FmApprovalAuthorityRuleMapper mapper;

    public FmApprovalAuthorityRuleServiceImpl(FmApprovalAuthorityRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmApprovalAuthorityRule, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public List<FmApprovalAuthorityRule> findByAuthority(
            String tenantId,
            String approvalAuthorityId) {
        return mapper.selectByAuthority(authorityParameters(
                tenantId,
                approvalAuthorityId));
    }

    @Override
    @Transactional(readOnly = false)
    public void replaceAuthority(
            String tenantId,
            String approvalAuthorityId,
            List<FmApprovalAuthorityRule> rules) {
        mapper.deleteByAuthority(authorityParameters(
                tenantId,
                approvalAuthorityId));
        rules.forEach(this::insert);
    }

    private Map<String, Object> authorityParameters(
            String tenantId,
            String approvalAuthorityId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tenantId", tenantId);
        parameters.put("approvalAuthorityId", approvalAuthorityId);
        return parameters;
    }
}
