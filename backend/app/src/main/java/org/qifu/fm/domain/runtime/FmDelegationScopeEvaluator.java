package org.qifu.fm.domain.runtime;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmWorkflowDelegation;

import tools.jackson.databind.ObjectMapper;

public class FmDelegationScopeEvaluator {

    private final ObjectMapper objectMapper;

    public FmDelegationScopeEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean applies(
            FmWorkflowDelegation delegation,
            String processDefId,
            String taskDefKey,
            List<FmTaskAssignmentRule> rules) {
        if (delegation == null) {
            return false;
        }
        if ("ALL".equals(delegation.getScopeType())) {
            return true;
        }
        if ("PROCESS".equals(delegation.getScopeType())) {
            return processDefId.equals(delegation.getScopeRefId());
        }
        if (!"APPROVAL_GROUP".equals(delegation.getScopeType())
                || StringUtils.isBlank(delegation.getScopeRefId())) {
            return false;
        }
        return rules.stream()
                .filter(rule -> taskDefKey.equals(rule.getTaskDefKey()))
                .filter(rule -> "ACTIVE".equals(rule.getStatus()))
                .filter(rule -> "APPROVAL_GROUP".equals(rule.getResolverType()))
                .anyMatch(rule -> delegation.getScopeRefId().equals(
                        approvalGroupId(rule.getResolverConfig())));
    }

    private String approvalGroupId(String resolverConfig) {
        try {
            return objectMapper.readTree(StringUtils.defaultIfBlank(
                    resolverConfig, "{}")).path("approvalGroupId").asString();
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
