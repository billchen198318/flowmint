package org.qifu.fm.domain.runtime;

import java.util.List;
import java.util.Set;

import org.qifu.fm.entity.FmProcessStartPolicy;
import org.springframework.stereotype.Component;

@Component
public class FmProcessStartPolicyEvaluator {

    public boolean isAllowed(
            List<FmProcessStartPolicy> policies,
            StartSubject subject) {
        if (policies == null || policies.isEmpty() || subject == null) {
            return false;
        }
        List<FmProcessStartPolicy> matched = policies.stream()
                .filter(policy -> matches(policy, subject))
                .toList();
        if (matched.isEmpty()) {
            return false;
        }
        if (matched.stream().anyMatch(policy -> "N".equals(policy.getAllowStart()))) {
            return false;
        }
        return matched.stream().anyMatch(policy -> "Y".equals(policy.getAllowStart()));
    }

    private boolean matches(FmProcessStartPolicy policy, StartSubject subject) {
        if (policy == null || policy.getSubjectType() == null) {
            return false;
        }
        return switch (policy.getSubjectType()) {
            case "ALL" -> true;
            case "ACCOUNT" -> subject.account().equals(policy.getSubjectRefId());
            case "ORG_UNIT" -> subject.orgUnitIds().contains(policy.getSubjectRefId());
            case "APPROVAL_GROUP" -> subject.approvalGroupIds()
                    .contains(policy.getSubjectRefId());
            default -> false;
        };
    }

    public record StartSubject(
            String account,
            Set<String> orgUnitIds,
            Set<String> approvalGroupIds) {

        public StartSubject {
            orgUnitIds = orgUnitIds == null ? Set.of() : Set.copyOf(orgUnitIds);
            approvalGroupIds = approvalGroupIds == null
                    ? Set.of() : Set.copyOf(approvalGroupIds);
        }
    }
}
