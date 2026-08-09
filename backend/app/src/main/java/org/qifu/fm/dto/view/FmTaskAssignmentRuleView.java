package org.qifu.fm.dto.view;

public record FmTaskAssignmentRuleView(
        String taskDefKey,
        Integer ruleSeq,
        String resolverType,
        String resolverConfig,
        String fallbackConfig,
        Integer maxResults,
        String status) {
}
