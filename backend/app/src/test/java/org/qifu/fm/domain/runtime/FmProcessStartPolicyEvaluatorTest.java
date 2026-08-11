package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.fm.domain.runtime.FmProcessStartPolicyEvaluator.StartSubject;
import org.qifu.fm.entity.FmProcessStartPolicy;

class FmProcessStartPolicyEvaluatorTest {

    private final FmProcessStartPolicyEvaluator evaluator =
            new FmProcessStartPolicyEvaluator();
    private final StartSubject subject = new StartSubject(
            "tester",
            Set.of("IT"),
            Set.of("PURCHASE_APPROVERS"));

    @Test
    void deniesWhenNoPolicyMatches() {
        assertFalse(evaluator.isAllowed(List.of(policy("ACCOUNT", "other", "Y")), subject));
        assertFalse(evaluator.isAllowed(List.of(), subject));
    }

    @Test
    void allowsMatchingAccountOrganizationOrGroup() {
        assertTrue(evaluator.isAllowed(List.of(policy("ACCOUNT", "tester", "Y")), subject));
        assertTrue(evaluator.isAllowed(List.of(policy("ORG_UNIT", "IT", "Y")), subject));
        assertTrue(evaluator.isAllowed(
                List.of(policy("APPROVAL_GROUP", "PURCHASE_APPROVERS", "Y")), subject));
    }

    @Test
    void allPolicyMatchesEverySubject() {
        assertTrue(evaluator.isAllowed(List.of(policy("ALL", null, "Y")), subject));
    }

    @Test
    void matchingDenyOverridesMatchingAllow() {
        assertFalse(evaluator.isAllowed(List.of(
                policy("ACCOUNT", "tester", "Y"),
                policy("ORG_UNIT", "IT", "N")), subject));
    }

    private FmProcessStartPolicy policy(
            String subjectType,
            String subjectRefId,
            String allowStart) {
        FmProcessStartPolicy policy = new FmProcessStartPolicy();
        policy.setSubjectType(subjectType);
        policy.setSubjectRefId(subjectRefId);
        policy.setAllowStart(allowStart);
        return policy;
    }
}
