package org.qifu.fm.dto.view;

public record FmOperationsDailyReportView(
        String reportDate,
        Long startedProcesses,
        Long completedProcesses,
        Long averageCompletedMinutes) {
}
