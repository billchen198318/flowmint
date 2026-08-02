package org.qifu.fm.dto.command;
import java.util.List;

public record FmProcessVersionCommand(
        String oid,
        String bpmnXml,
        List<FmTaskFormRuleCommand> taskForms) {
}
