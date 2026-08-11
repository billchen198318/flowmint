package org.qifu.fm.dto.view;

import java.util.Date;

public record FmOperationsReportView(
        Date startDate,
        Date endDate,
        Long totalProcesses,
        Long runningProcesses,
        Long completedProcesses,
        Long rejectedProcesses,
        Long cancelledProcesses,
        Long terminatedProcesses,
        Long averageCompletedMinutes,
        Long overdueTasks,
        Long dueSoonTasks) {
}
