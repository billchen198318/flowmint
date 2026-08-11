package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskAssignmentSnapshotDtl implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String assignmentSnapshotId;
	private Integer resultSeq;
	private String resultType;
	private String resultAccount;
	private String principalAccount;
	private String orgUnitId;
	private String orgUnitName;
	private String approvalLevelId;
	private String levelCode;
	private String levelName;
	private Integer levelOrder;
	private String resolutionPath;
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
	public Integer getResultSeq() {
		return resultSeq;
	}
	public void setResultSeq(Integer resultSeq) {
		this.resultSeq = resultSeq;
	}
	public String getResultType() {
		return resultType;
	}
	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	public String getResultAccount() {
		return resultAccount;
	}
	public void setResultAccount(String resultAccount) {
		this.resultAccount = resultAccount;
	}
	public String getPrincipalAccount() {
		return principalAccount;
	}
	public void setPrincipalAccount(String principalAccount) {
		this.principalAccount = principalAccount;
	}
	public String getOrgUnitId() {
		return orgUnitId;
	}
	public void setOrgUnitId(String orgUnitId) {
		this.orgUnitId = orgUnitId;
	}
	public String getOrgUnitName() {
		return orgUnitName;
	}
	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}
	public String getApprovalLevelId() {
		return approvalLevelId;
	}
	public void setApprovalLevelId(String approvalLevelId) {
		this.approvalLevelId = approvalLevelId;
	}
	public String getLevelCode() {
		return levelCode;
	}
	public void setLevelCode(String levelCode) {
		this.levelCode = levelCode;
	}
	public String getLevelName() {
		return levelName;
	}
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	public Integer getLevelOrder() {
		return levelOrder;
	}
	public void setLevelOrder(Integer levelOrder) {
		this.levelOrder = levelOrder;
	}
	public String getResolutionPath() {
		return resolutionPath;
	}
	public void setResolutionPath(String resolutionPath) {
		this.resolutionPath = resolutionPath;
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
