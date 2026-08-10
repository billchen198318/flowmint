package org.qifu.fm.dto.view;

import java.util.List;
import java.util.Map;

public record FmTaskDetailView(
        FmTaskInboxView task,
        String formId,
        Integer formVersionNo,
        String formName,
        String schemaContent,
        String uiSchemaContent,
        String customScriptContent,
        Map<String, Object> formData,
        boolean correctionTask,
        boolean allowReject,
        boolean allowReturn,
        boolean allowTransfer,
        boolean delegatedTask,
        List<FmOptionView> delegationOptions,
        String commentRequired,
        List<FmTaskHistoryView> returnTargets,
        List<FmTaskActionView> actions) {
}
