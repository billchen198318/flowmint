package org.qifu.fm.dto.command;

public record FmTaskPolicyCommand(
        String taskDefKey,
        String taskName,
        String assignmentMode,
        String selfApprovalPolicy,
        String duplicatePolicy,
        String allowReject,
        String allowReturn,
        String allowTransfer,
        String allowAddSign,
        String commentRequired) {
}
