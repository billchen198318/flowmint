package org.qifu.fm.dto.view;

public record FmOperationsProcessRankingView(
        String processDefId,
        String processName,
        Long startedProcesses,
        Long completedProcesses,
        Double completionRate,
        Long averageCompletedMinutes) {
}
