package org.qifu.fm.dto.view;

public record FmTaskReassignPreviewView(
        String taskId,
        String taskName,
        String previousAssignee,
        String targetAccount,
        String targetDisplayName,
        String assignmentMode,
        boolean multiInstance,
        String warning) {
}
