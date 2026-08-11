package org.qifu.fm.dto.command;

public record FmFormVersionCommand(
        String oid,
        String schemaContent,
        String uiSchemaContent,
        String customScriptContent) {
}
