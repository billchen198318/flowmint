package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmTaskAssignmentRule;

public interface IFmTaskAssignmentRuleService extends IBaseService<FmTaskAssignmentRule, String> {

    List<FmTaskAssignmentRule> findByVersion(
            String tenantId,
            String processDefId,
            Integer versionNo);

    void replaceVersion(
            String tenantId,
            String processDefId,
            Integer versionNo,
            List<FmTaskAssignmentRule> rules);
}
