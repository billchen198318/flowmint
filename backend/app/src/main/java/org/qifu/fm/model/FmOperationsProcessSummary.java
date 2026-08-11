package org.qifu.fm.model;

public class FmOperationsProcessSummary {

    private Long totalProcesses;
    private Long runningProcesses;
    private Long completedProcesses;
    private Long rejectedProcesses;
    private Long cancelledProcesses;
    private Long terminatedProcesses;
    private Long averageCompletedMinutes;

    public Long getTotalProcesses() {
        return totalProcesses;
    }

    public void setTotalProcesses(Long value) {
        this.totalProcesses = value;
    }

    public Long getRunningProcesses() {
        return runningProcesses;
    }

    public void setRunningProcesses(Long value) {
        this.runningProcesses = value;
    }

    public Long getCompletedProcesses() {
        return completedProcesses;
    }

    public void setCompletedProcesses(Long value) {
        this.completedProcesses = value;
    }

    public Long getRejectedProcesses() {
        return rejectedProcesses;
    }

    public void setRejectedProcesses(Long value) {
        this.rejectedProcesses = value;
    }

    public Long getCancelledProcesses() {
        return cancelledProcesses;
    }

    public void setCancelledProcesses(Long value) {
        this.cancelledProcesses = value;
    }

    public Long getTerminatedProcesses() {
        return terminatedProcesses;
    }

    public void setTerminatedProcesses(Long value) {
        this.terminatedProcesses = value;
    }

    public Long getAverageCompletedMinutes() {
        return averageCompletedMinutes;
    }

    public void setAverageCompletedMinutes(Long value) {
        this.averageCompletedMinutes = value;
    }
}
