package org.qifu.fm.dto.view;

public record FmTaskPolicyView(
        String taskDefKey,
        String taskName,
        String assignmentMode,
        String selfApprovalPolicy,
        String duplicatePolicy,
        String allowReject,
        String allowReturn,
        String allowTransfer,
        String allowAddSign,
        String commentRequired,
        Integer dueHours,
        Integer reminderBeforeHours) {
}
