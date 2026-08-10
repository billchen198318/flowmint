package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;

import tools.jackson.databind.ObjectMapper;

class FmApprovalGroupModeValidatorTest {

    private final FmApprovalGroupModeValidator validator =
            new FmApprovalGroupModeValidator(new ObjectMapper());

    @Test
    void acceptsMatchingCandidateAllAndSequentialModes() {
        assertDoesNotThrow(() -> validator.validate(
                List.of(policy("a", "CANDIDATE"), policy("b", "ALL"),
                        policy("c", "SEQUENTIAL")),
                List.of(rule("a", "G1"), rule("b", "G2"), rule("c", "G3")),
                Map.of("G1", "CANDIDATE", "G2", "ALL", "G3", "SEQUENTIAL")));
    }

    @Test
    void rejectsPolicyModeThatDiffersFromGroup() {
        assertThrows(ServiceException.class, () -> validator.validate(
                List.of(policy("approval", "CANDIDATE")),
                List.of(rule("approval", "G1")), Map.of("G1", "ALL")));
    }

    @Test
    void rejectsMissingInactiveOrMalformedGroupReferences() {
        assertThrows(ServiceException.class, () -> validator.validate(
                List.of(policy("approval", "ALL")),
                List.of(rule("approval", "MISSING")), Map.of()));
        FmTaskAssignmentRule malformed = rule("approval", "G1");
        malformed.setResolverConfig("not-json");
        assertThrows(ServiceException.class, () -> validator.validate(
                List.of(policy("approval", "ALL")), List.of(malformed),
                Map.of("G1", "ALL")));
    }

    private FmTaskPolicy policy(String taskKey, String mode) {
        FmTaskPolicy value = new FmTaskPolicy();
        value.setTaskDefKey(taskKey);
        value.setAssignmentMode(mode);
        return value;
    }

    private FmTaskAssignmentRule rule(String taskKey, String groupId) {
        FmTaskAssignmentRule value = new FmTaskAssignmentRule();
        value.setTaskDefKey(taskKey);
        value.setResolverType("APPROVAL_GROUP");
        value.setResolverConfig("{\"approvalGroupId\":\"" + groupId + "\"}");
        value.setStatus("ACTIVE");
        return value;
    }
}
