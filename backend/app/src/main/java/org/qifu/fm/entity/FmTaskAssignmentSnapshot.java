package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskAssignmentSnapshot implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String assignmentSnapshotId;
	private String processInstanceId;
	private String taskId;
	private String taskDefKey;
	private Integer resolutionSeq;
	private String resolverType;
	private String sourceAccount;
	private String sourceOrgUnitId;
	private String resolutionStatus;
	private String resolutionContext;
	private Date resolvedDate;
	private Date supersededDate;
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
	public String getAssignmentSnapshotId() {
		return assignmentSnapshotId;
	}
	public void setAssignmentSnapshotId(String assignmentSnapshotId) {
		this.assignmentSnapshotId = assignmentSnapshotId;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getTaskDefKey() {
		return taskDefKey;
	}
	public void setTaskDefKey(String taskDefKey) {
		this.taskDefKey = taskDefKey;
	}
	public Integer getResolutionSeq() {
		return resolutionSeq;
	}
	public void setResolutionSeq(Integer resolutionSeq) {
		this.resolutionSeq = resolutionSeq;
	}
	public String getResolverType() {
		return resolverType;
	}
	public void setResolverType(String resolverType) {
		this.resolverType = resolverType;
	}
	public String getSourceAccount() {
		return sourceAccount;
	}
	public void setSourceAccount(String sourceAccount) {
		this.sourceAccount = sourceAccount;
	}
	public String getSourceOrgUnitId() {
		return sourceOrgUnitId;
	}
	public void setSourceOrgUnitId(String sourceOrgUnitId) {
		this.sourceOrgUnitId = sourceOrgUnitId;
	}
	public String getResolutionStatus() {
		return resolutionStatus;
	}
	public void setResolutionStatus(String resolutionStatus) {
		this.resolutionStatus = resolutionStatus;
	}
	public String getResolutionContext() {
		return resolutionContext;
	}
	public void setResolutionContext(String resolutionContext) {
		this.resolutionContext = resolutionContext;
	}
	public Date getResolvedDate() {
		return resolvedDate;
	}
	public void setResolvedDate(Date resolvedDate) {
		this.resolvedDate = resolvedDate;
	}
	public Date getSupersededDate() {
		return supersededDate;
	}
	public void setSupersededDate(Date supersededDate) {
		this.supersededDate = supersededDate;
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
