package org.qifu.fm.dto.view;

public record FmProcessStartPolicyView(
        Integer policySeq,
        String subjectType,
        String subjectRefId,
        String allowStart) {
}
