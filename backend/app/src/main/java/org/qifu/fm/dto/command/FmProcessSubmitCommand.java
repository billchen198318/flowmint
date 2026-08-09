package org.qifu.fm.dto.command;

import java.util.Map;

public record FmProcessSubmitCommand(
        String tenantId,
        String processDefId,
        String formId,
        Integer formVersionNo,
        String businessKey,
        String applicantAccount,
        Map<String, Object> formData) {
}
