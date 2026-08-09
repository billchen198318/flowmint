package org.qifu.fm.dto.view;

import java.util.List;
import java.util.Map;

public record FmRequestTrackDetailView(
        FmRequestTrackView request,
        String formId,
        Integer formVersionNo,
        String schemaContent,
        String uiSchemaContent,
        String customScriptContent,
        Map<String, Object> currentFormData,
        List<FmTaskActionView> actions,
        List<FmFormSnapshotView> snapshots) {
}
