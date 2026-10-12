package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.flowable.task.service.delegate.DelegateTask;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmTaskPolicy;

class FmTaskAssignmentNotificationRecipientsTest {

    @Test
    void multiInstanceTaskNotifiesOnlyItsCurrentAssignee() {
        DelegateTask task = mock(DelegateTask.class);
        when(task.getAssignee()).thenReturn("second");
        Set<String> allMembers = Set.of("first", "second", "third");

        for (String mode : List.of("ALL", "SEQUENTIAL", "ASSIGNEE")) {
            FmTaskPolicy policy = new FmTaskPolicy();
            policy.setAssignmentMode(mode);
            assertEquals(List.of("second"),
                    FmTaskAssignmentListener.assignmentNotificationRecipients(
                            task, policy, allMembers));
        }
    }

    @Test
    void candidateTaskNotifiesAllCurrentCandidates() {
        DelegateTask task = mock(DelegateTask.class);
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setAssignmentMode("CANDIDATE");

        assertEquals(Set.of("first", "second"), Set.copyOf(
                FmTaskAssignmentListener.assignmentNotificationRecipients(
                        task, policy, Set.of("first", "second"))));
    }
}
