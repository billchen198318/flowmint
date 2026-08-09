package org.qifu.fm.dto.command;

public record FmTaskAssignmentRuleCommand(
        String taskDefKey,
        Integer ruleSeq,
        String resolverType,
        String resolverConfig,
        String fallbackConfig,
        Integer maxResults,
        String status) {
}
