package org.qifu.fm.model;

public class FmOperationsProcessRanking {

    private String processDefId;
    private String processName;
    private Long startedProcesses;
    private Long completedProcesses;
    private Long averageCompletedMinutes;

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
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
