package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String processDefId;
    private Integer processVersionNo;
    private String taskDefKey;
    private String taskName;
    private String assignmentMode;
    private String selfApprovalPolicy;
    private String duplicatePolicy;
    private String allowReject;
    private String allowReturn;
    private String allowTransfer;
    private String allowAddSign;
    private String commentRequired;
    private Integer dueHours;
    private Integer reminderBeforeHours;
    private String cuserid;
    private Date cdate;
    private String uuserid;
    private Date udate;

    @EntityPK(name = "oid", autoUUID = true)
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public Integer getProcessVersionNo() {
        return processVersionNo;
    }

    public void setProcessVersionNo(Integer processVersionNo) {
        this.processVersionNo = processVersionNo;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssignmentMode() {
        return assignmentMode;
    }

    public void setAssignmentMode(String assignmentMode) {
        this.assignmentMode = assignmentMode;
    }

    public String getSelfApprovalPolicy() {
        return selfApprovalPolicy;
    }

    public void setSelfApprovalPolicy(String selfApprovalPolicy) {
        this.selfApprovalPolicy = selfApprovalPolicy;
    }

    public String getDuplicatePolicy() {
        return duplicatePolicy;
    }

    public void setDuplicatePolicy(String duplicatePolicy) {
        this.duplicatePolicy = duplicatePolicy;
    }

    public String getAllowReject() {
        return allowReject;
    }

    public void setAllowReject(String allowReject) {
        this.allowReject = allowReject;
    }

    public String getAllowReturn() {
        return allowReturn;
    }

    public void setAllowReturn(String allowReturn) {
        this.allowReturn = allowReturn;
    }

    public String getAllowTransfer() {
        return allowTransfer;
    }

    public void setAllowTransfer(String allowTransfer) {
        this.allowTransfer = allowTransfer;
    }

    public String getAllowAddSign() {
        return allowAddSign;
    }

    public void setAllowAddSign(String allowAddSign) {
        this.allowAddSign = allowAddSign;
    }

    public String getCommentRequired() {
        return commentRequired;
    }

    public void setCommentRequired(String commentRequired) {
        this.commentRequired = commentRequired;
    }

    public Integer getDueHours() {
        return dueHours;
    }

    public void setDueHours(Integer dueHours) {
        this.dueHours = dueHours;
    }

    public Integer getReminderBeforeHours() {
        return reminderBeforeHours;
    }

    public void setReminderBeforeHours(Integer reminderBeforeHours) {
        this.reminderBeforeHours = reminderBeforeHours;
    }

    @CreateUserField(name = "cuserid")
    public String getCuserid() {
        return cuserid;
    }

    public void setCuserid(String cuserid) {
        this.cuserid = cuserid;
    }

    @CreateDateField(name = "cdate")
    public Date getCdate() {
        return cdate;
    }

    public void setCdate(Date cdate) {
        this.cdate = cdate;
    }

    @UpdateUserField(name = "uuserid")
    public String getUuserid() {
        return uuserid;
    }

    public void setUuserid(String uuserid) {
        this.uuserid = uuserid;
    }

    @UpdateDateField(name = "udate")
    public Date getUdate() {
        return udate;
    }

    public void setUdate(Date udate) {
        this.udate = udate;
    }
}
