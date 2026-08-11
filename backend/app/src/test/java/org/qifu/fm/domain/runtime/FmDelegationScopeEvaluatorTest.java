package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmWorkflowDelegation;

import tools.jackson.databind.ObjectMapper;

class FmDelegationScopeEvaluatorTest {

    private final FmDelegationScopeEvaluator evaluator =
            new FmDelegationScopeEvaluator(new ObjectMapper());

    @Test
    void matchesAllAndCurrentProcessScopes() {
        assertTrue(evaluator.applies(delegation("ALL", null),
                "PURCHASE", "manager", List.of()));
        assertTrue(evaluator.applies(delegation("PROCESS", "PURCHASE"),
                "PURCHASE", "manager", List.of()));
        assertFalse(evaluator.applies(delegation("PROCESS", "LEAVE"),
                "PURCHASE", "manager", List.of()));
    }

    @Test
    void matchesApprovalGroupOnlyOnActiveRuleForCurrentTask() {
        assertTrue(evaluator.applies(delegation("APPROVAL_GROUP", "FINANCE"),
                "PURCHASE", "finance", List.of(rule(
                        "finance", "ACTIVE", "APPROVAL_GROUP",
                        "{\"approvalGroupId\":\"FINANCE\"}"))));
        assertFalse(evaluator.applies(delegation("APPROVAL_GROUP", "FINANCE"),
                "PURCHASE", "manager", List.of(rule(
                        "finance", "ACTIVE", "APPROVAL_GROUP",
                        "{\"approvalGroupId\":\"FINANCE\"}"))));
        assertFalse(evaluator.applies(delegation("APPROVAL_GROUP", "FINANCE"),
                "PURCHASE", "finance", List.of(rule(
                        "finance", "INACTIVE", "APPROVAL_GROUP",
                        "{\"approvalGroupId\":\"FINANCE\"}"))));
    }

    @Test
    void rejectsDifferentResolverAndInvalidConfiguration() {
        assertFalse(evaluator.applies(delegation("APPROVAL_GROUP", "FINANCE"),
                "PURCHASE", "finance", List.of(rule(
                        "finance", "ACTIVE", "FIXED_ACCOUNT",
                        "{\"approvalGroupId\":\"FINANCE\"}"))));
        assertFalse(evaluator.applies(delegation("APPROVAL_GROUP", "FINANCE"),
                "PURCHASE", "finance", List.of(rule(
                        "finance", "ACTIVE", "APPROVAL_GROUP", "not-json"))));
    }

    private FmWorkflowDelegation delegation(String scopeType, String scopeRefId) {
        FmWorkflowDelegation value = new FmWorkflowDelegation();
        value.setScopeType(scopeType);
        value.setScopeRefId(scopeRefId);
        return value;
    }

    private FmTaskAssignmentRule rule(
            String taskDefKey, String status, String resolverType,
            String resolverConfig) {
        FmTaskAssignmentRule value = new FmTaskAssignmentRule();
        value.setTaskDefKey(taskDefKey);
        value.setStatus(status);
        value.setResolverType(resolverType);
        value.setResolverConfig(resolverConfig);
        return value;
    }
}
