package org.qifu.fm.dto.command;

public record FmProcessStartCatalogCommand(
        String tenantId,
        String applicantAccount) {
}
