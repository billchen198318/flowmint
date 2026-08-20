package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.entity.FmTaskPolicy;

class FmProcessPublishValidatorTest {

    private final FmProcessPublishValidator validator = new FmProcessPublishValidator();

    @Test
    void acceptsCompleteTaskConfiguration() {
        assertDoesNotThrow(() -> validator.validate(Set.of("review", "correction"),
                List.of(form("review"), form("correction")),
                List.of(policy("review", "SINGLE", "Y"),
                        policy("correction", "APPLICANT_CORRECTION", "N")),
                List.of(activeRule("review"))));
    }

    @Test
    void rejectsMissingAndDuplicateConfiguration() {
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(), List.of(policy("review", "SINGLE", "N")),
                List.of(activeRule("review"))));
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(form("review"), form("review")),
                List.of(policy("review", "SINGLE", "N")),
                List.of(activeRule("review"))));
    }

    @Test
    void rejectsStaleTaskConfiguration() {
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(form("review"), form("removedTask")),
                List.of(policy("review", "SINGLE", "N")),
                List.of(activeRule("review"))));
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(form("review")),
                List.of(policy("review", "SINGLE", "N")),
                List.of(activeRule("review"), activeRule("removedTask"))));
    }

    @Test
    void rejectsReturnWithoutCorrectionTaskAndMissingActiveAssignment() {
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(form("review")),
                List.of(policy("review", "SINGLE", "Y")),
                List.of(activeRule("review"))));
        assertThrows(ServiceException.class, () -> validator.validate(Set.of("review"),
                List.of(form("review")),
                List.of(policy("review", "SINGLE", "N")), List.of()));
    }

    private FmTaskFormRule form(String taskKey) {
        FmTaskFormRule value = new FmTaskFormRule();
        value.setTaskDefKey(taskKey);
        return value;
    }

    private FmTaskPolicy policy(String taskKey, String assignmentMode, String allowReturn) {
        FmTaskPolicy value = new FmTaskPolicy();
        value.setTaskDefKey(taskKey);
        value.setAssignmentMode(assignmentMode);
        value.setAllowReturn(allowReturn);
        return value;
    }

    private FmTaskAssignmentRule activeRule(String taskKey) {
        FmTaskAssignmentRule value = new FmTaskAssignmentRule();
        value.setTaskDefKey(taskKey);
        value.setStatus("ACTIVE");
        return value;
    }
}
