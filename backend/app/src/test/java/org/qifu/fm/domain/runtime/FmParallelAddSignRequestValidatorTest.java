package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.entity.FmTaskPolicy;

class FmParallelAddSignRequestValidatorTest {

    private final FmParallelAddSignRequestValidator validator =
            new FmParallelAddSignRequestValidator();

    @Test
    void acceptsDistinctMembersWithinPublishedPolicyLimit() throws Exception {
        List<String> members = validator.validateStart(
                new FmParallelAddSignStartRequest(
                        "TASK-1", List.of(" user2 ", "user3"),
                        "需要專業意見", "REQ-1"),
                policy("Y", 2), "user1");
        assertEquals(List.of("user2", "user3"), members);
    }

    @Test
    void rejectsDisabledPolicyDuplicateSelfAndOverLimit() {
        assertThrows(ServiceException.class, () -> validator.validateStart(
                request(List.of("user2")), policy("N", 10), "user1"));
        assertThrows(ServiceException.class, () -> validator.validateStart(
                request(List.of("user2", "user2")), policy("Y", 10), "user1"));
        assertThrows(ServiceException.class, () -> validator.validateStart(
                request(List.of("user1")), policy("Y", 10), "user1"));
        assertThrows(ServiceException.class, () -> validator.validateStart(
                request(List.of("user2", "user3")), policy("Y", 1), "user1"));
    }

    @Test
    void validatesAgreeDisagreeAndRequiredComment() throws Exception {
        assertEquals("同意", validator.validateComplete(
                new FmParallelAddSignCompleteRequest(
                        "SUBTASK-1", "AGREE", " 同意 "), true));
        assertThrows(ServiceException.class, () -> validator.validateComplete(
                new FmParallelAddSignCompleteRequest(
                        "SUBTASK-1", "DISAGREE", " "), true));
        assertThrows(ServiceException.class, () -> validator.validateComplete(
                new FmParallelAddSignCompleteRequest(
                        "SUBTASK-1", "APPROVE", "意見"), true));
    }

    private FmParallelAddSignStartRequest request(List<String> members) {
        return new FmParallelAddSignStartRequest(
                "TASK-1", members, "原因", "REQ-1");
    }

    private FmTaskPolicy policy(String allowed, int maximum) {
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setAllowParallelAddSign(allowed);
        policy.setParallelAddSignMaxMembers(maximum);
        policy.setParallelAddSignCommentRequired("Y");
        return policy;
    }
}
