package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmApprovalAuthorityRule;

public interface IFmApprovalAuthorityRuleService extends IBaseService<FmApprovalAuthorityRule, String> {

    List<FmApprovalAuthorityRule> findByAuthority(
            String tenantId,
            String approvalAuthorityId);

    void replaceAuthority(
            String tenantId,
            String approvalAuthorityId,
            List<FmApprovalAuthorityRule> rules);
}
