package org.qifu.fm.dto.view;

public record FmApprovalAuthorityRuleView(
        String oid,
        String approvalAuthorityRuleId,
        Integer ruleSeq,
        String conditionConfig,
        String targetType,
        String targetRefId,
        String targetLabel,
        String resolverConfig,
        String stopAfterApproval,
        String status) {
}
