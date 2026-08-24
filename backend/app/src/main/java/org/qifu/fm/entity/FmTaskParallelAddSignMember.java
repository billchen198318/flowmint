package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskParallelAddSignMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String parallelAddSignOid;
    private String memberAccount;
    private String flowableTaskId;
    private String status;
    private String comment;
    private Date completedDate;
    private String originalMemberAccount;
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
    public String getParallelAddSignOid() { return parallelAddSignOid; }
    public void setParallelAddSignOid(String parallelAddSignOid) { this.parallelAddSignOid = parallelAddSignOid; }
    public String getMemberAccount() { return memberAccount; }
    public void setMemberAccount(String memberAccount) { this.memberAccount = memberAccount; }
    public String getFlowableTaskId() { return flowableTaskId; }
    public void setFlowableTaskId(String flowableTaskId) { this.flowableTaskId = flowableTaskId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Date getCompletedDate() { return completedDate; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }
    public String getOriginalMemberAccount() { return originalMemberAccount; }
    public void setOriginalMemberAccount(String originalMemberAccount) { this.originalMemberAccount = originalMemberAccount; }
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
