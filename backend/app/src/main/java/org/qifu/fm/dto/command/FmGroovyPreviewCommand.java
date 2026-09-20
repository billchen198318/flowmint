package org.qifu.fm.dto.command;

import java.util.List;

/** Unsaved design plus manual sample only; no tenant, profile or trusted context from the client. */
public record FmGroovyPreviewCommand(String oid, Integer expectedLockVersion, String bpmnXml,
        List<FmGroovyBindingCommand> groovyBindings, String nodeId, String sampleInput) {
}
