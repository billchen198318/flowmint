package org.qifu.fm.dto.command;

public record FmProcessVersionCommand(
        String oid,
        String bpmnXml) {
}