package org.qifu.fm.dto.command;

public record FmProcessStartPolicyCommand(
        Integer policySeq,
        String subjectType,
        String subjectRefId,
        String allowStart) {
}
