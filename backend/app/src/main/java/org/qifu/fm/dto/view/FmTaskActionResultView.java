package org.qifu.fm.dto.view;

public record FmTaskActionResultView(
        String taskId,
        String actionType,
        String processInstanceId,
        String instanceStatus) {
}
