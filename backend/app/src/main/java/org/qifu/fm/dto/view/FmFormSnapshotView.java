package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.Map;

public record FmFormSnapshotView(
        String formSnapshotId,
        String taskId,
        String actionType,
        Integer revisionNo,
        String contentSha256,
        Date snapshotDate,
        Map<String, Object> formData) {
}
