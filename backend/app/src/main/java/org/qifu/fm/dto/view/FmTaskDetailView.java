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
        boolean allowReject,
        boolean allowReturn,
        String commentRequired,
        List<FmTaskHistoryView> returnTargets,
        List<FmTaskActionView> actions) {
}
