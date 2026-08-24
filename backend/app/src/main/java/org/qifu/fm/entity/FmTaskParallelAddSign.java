package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskParallelAddSign implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String processInstanceId;
    private String parentTaskId;
    private String taskDefinitionKey;
    private Integer batchNo;
    private String requestKey;
    private String status;
    private String initiatorAccount;
    private String reason;
    private Integer totalCount;
    private Integer completedCount;
    private Integer agreeCount;
    private Integer disagreeCount;
    private String formSnapshotOid;
    private Date startedDate;
    private Date completedDate;
    private Date cancelledDate;
    private Integer lockVersion;
    private String cuserid;
    private Date cdate;
    private String uuserid;
    private Date udate;

    @EntityPK(name = "oid", autoUUID = true)
    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(String parentTaskId) { this.parentTaskId = parentTaskId; }
    public String getTaskDefinitionKey() { return taskDefinitionKey; }
    public void setTaskDefinitionKey(String taskDefinitionKey) { this.taskDefinitionKey = taskDefinitionKey; }
    public Integer getBatchNo() { return batchNo; }
    public void setBatchNo(Integer batchNo) { this.batchNo = batchNo; }
    public String getRequestKey() { return requestKey; }
    public void setRequestKey(String requestKey) { this.requestKey = requestKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInitiatorAccount() { return initiatorAccount; }
    public void setInitiatorAccount(String initiatorAccount) { this.initiatorAccount = initiatorAccount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    public Integer getAgreeCount() { return agreeCount; }
    public void setAgreeCount(Integer agreeCount) { this.agreeCount = agreeCount; }
    public Integer getDisagreeCount() { return disagreeCount; }
    public void setDisagreeCount(Integer disagreeCount) { this.disagreeCount = disagreeCount; }
    public String getFormSnapshotOid() { return formSnapshotOid; }
    public void setFormSnapshotOid(String formSnapshotOid) { this.formSnapshotOid = formSnapshotOid; }
    public Date getStartedDate() { return startedDate; }
    public void setStartedDate(Date startedDate) { this.startedDate = startedDate; }
    public Date getCompletedDate() { return completedDate; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }
    public Date getCancelledDate() { return cancelledDate; }
    public void setCancelledDate(Date cancelledDate) { this.cancelledDate = cancelledDate; }
    public Integer getLockVersion() { return lockVersion; }
    public void setLockVersion(Integer lockVersion) { this.lockVersion = lockVersion; }
    @CreateUserField(name = "cuserid")
    public String getCuserid() { return cuserid; }
    public void setCuserid(String cuserid) { this.cuserid = cuserid; }
    @CreateDateField(name = "cdate")
    public Date getCdate() { return cdate; }
    public void setCdate(Date cdate) { this.cdate = cdate; }
    @UpdateUserField(name = "uuserid")
    public String getUuserid() { return uuserid; }
    public void setUuserid(String uuserid) { this.uuserid = uuserid; }
    @UpdateDateField(name = "udate")
    public Date getUdate() { return udate; }
    public void setUdate(Date udate) { this.udate = udate; }
}
