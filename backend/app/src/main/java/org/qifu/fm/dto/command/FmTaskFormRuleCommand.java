package org.qifu.fm.dto.command;

public record FmTaskFormRuleCommand(
        String taskDefKey,
        String formId,
        Integer formVersionNo) {
}
