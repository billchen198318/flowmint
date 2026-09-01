package org.qifu.fm.dto.command;

import java.util.Map;

public record FmTaskActionRequest(
        String taskId,
        String actionType,
        String comment,
        String reason,
        String targetTaskDefKey,
        Integer formRevisionNo,
        Map<String, Object> formData) {
}
