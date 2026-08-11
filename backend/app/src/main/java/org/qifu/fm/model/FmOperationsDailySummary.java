package org.qifu.fm.model;

public class FmOperationsDailySummary {

    private String reportDate;
    private Long startedProcesses;
    private Long completedProcesses;
    private Long averageCompletedMinutes;

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public Long getStartedProcesses() {
        return startedProcesses;
    }

    public void setStartedProcesses(Long startedProcesses) {
        this.startedProcesses = startedProcesses;
    }

    public Long getCompletedProcesses() {
        return completedProcesses;
    }

    public void setCompletedProcesses(Long completedProcesses) {
        this.completedProcesses = completedProcesses;
    }

    public Long getAverageCompletedMinutes() {
        return averageCompletedMinutes;
    }

    public void setAverageCompletedMinutes(Long averageCompletedMinutes) {
        this.averageCompletedMinutes = averageCompletedMinutes;
    }
}
