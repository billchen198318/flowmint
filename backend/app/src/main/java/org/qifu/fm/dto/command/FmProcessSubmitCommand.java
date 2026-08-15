package org.qifu.fm.dto.command;

import java.util.Map;

public record FmProcessSubmitCommand(
        String tenantId,
        String processDefId,
        String formId,
        Integer formVersionNo,
        String idempotencyKey,
        String applicantAccount,
        Map<String, Object> formData,
        String uploadSessionId) {
}
