package org.qifu.fm.model;

public class FmOperationsTaskRanking {

    private String processDefId;
    private String processName;
    private String taskDefKey;
    private String taskName;
    private Long completedTasks;
    private Long averageHandlingMinutes;
    private Long maximumHandlingMinutes;

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String value) {
        this.processDefId = value;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String value) {
        this.processName = value;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(String value) {
        this.taskDefKey = value;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String value) {
        this.taskName = value;
    }

    public Long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Long value) {
        this.completedTasks = value;
    }

    public Long getAverageHandlingMinutes() {
        return averageHandlingMinutes;
    }

    public void setAverageHandlingMinutes(Long value) {
        this.averageHandlingMinutes = value;
    }

    public Long getMaximumHandlingMinutes() {
        return maximumHandlingMinutes;
    }

    public void setMaximumHandlingMinutes(Long value) {
        this.maximumHandlingMinutes = value;
    }
}
