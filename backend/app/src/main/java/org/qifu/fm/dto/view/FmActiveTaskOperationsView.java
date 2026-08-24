package org.qifu.fm.dto.view;

import java.util.Date;

public record FmActiveTaskOperationsView(
        String taskId,
        String taskDefinitionKey,
        String taskName,
        String assignee,
        String owner,
        String assignmentMode,
        Date dueDate,
        boolean parallelAddSign,
        boolean reassignable,
        String blockedReason) {
}
