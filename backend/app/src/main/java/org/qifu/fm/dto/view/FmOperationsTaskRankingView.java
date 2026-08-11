package org.qifu.fm.dto.view;

public record FmOperationsTaskRankingView(
        String processDefId,
        String processName,
        String taskDefKey,
        String taskName,
        Long completedTasks,
        Long averageHandlingMinutes,
        Long maximumHandlingMinutes) {
}
