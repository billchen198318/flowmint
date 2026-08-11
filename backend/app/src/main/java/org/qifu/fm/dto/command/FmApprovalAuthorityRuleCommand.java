package org.qifu.fm.dto.command;

public record FmApprovalAuthorityRuleCommand(
        String oid,
        Integer ruleSeq,
        String conditionConfig,
        String targetType,
        String targetRefId,
        String resolverConfig,
        String stopAfterApproval,
        String status) {
}
