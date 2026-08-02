package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmProcessVersion implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String processDefId;
	private Integer versionNo;
	private String versionStatus;
	private String bpmnXml;
	private String bpmnSha256;
	private String flowableDeploymentId;
	private String flowableProcessDefId;
	private String publishedBy;
	private Date publishedDate;
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

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public String getVersionStatus() {
		return versionStatus;
	}

	public void setVersionStatus(String versionStatus) {
		this.versionStatus = versionStatus;
	}

	public String getBpmnXml() {
		return bpmnXml;
	}

	public void setBpmnXml(String bpmnXml) {
		this.bpmnXml = bpmnXml;
	}

	public String getBpmnSha256() {
		return bpmnSha256;
	}

	public void setBpmnSha256(String bpmnSha256) {
		this.bpmnSha256 = bpmnSha256;
	}

	public String getFlowableDeploymentId() {
		return flowableDeploymentId;
	}

	public void setFlowableDeploymentId(String flowableDeploymentId) {
		this.flowableDeploymentId = flowableDeploymentId;
	}

	public String getFlowableProcessDefId() {
		return flowableProcessDefId;
	}

	public void setFlowableProcessDefId(String flowableProcessDefId) {
		this.flowableProcessDefId = flowableProcessDefId;
	}

	public String getPublishedBy() {
		return publishedBy;
	}

	public void setPublishedBy(String publishedBy) {
		this.publishedBy = publishedBy;
	}

	public Date getPublishedDate() {
		return publishedDate;
	}

	public void setPublishedDate(Date publishedDate) {
		this.publishedDate = publishedDate;
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
