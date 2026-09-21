package org.qifu.fm.dto.command;

public record FmGroovyBindingCommand(
        String nodeId,
        String bindingId,
        String scriptContent,
        String inputSchema,
        String outputSchema,
        String mappingContent,
        Integer timeoutMs) {
}
