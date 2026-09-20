package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.EntityPK;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.CreateDateField;

public class FmProcessGroovyManifest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String processDefId;
	private Integer versionNo;
	private String manifestContent;
	private String manifestSha256;
	private String bpmnSha256;
	private String engineProfile;
	private String cuserid;
	private Date cdate;

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

	public String getManifestContent() {
		return manifestContent;
	}

	public void setManifestContent(String manifestContent) {
		this.manifestContent = manifestContent;
	}

	public String getManifestSha256() {
		return manifestSha256;
	}

	public void setManifestSha256(String manifestSha256) {
		this.manifestSha256 = manifestSha256;
	}

	public String getBpmnSha256() {
		return bpmnSha256;
	}

	public void setBpmnSha256(String bpmnSha256) {
		this.bpmnSha256 = bpmnSha256;
	}

	public String getEngineProfile() {
		return engineProfile;
	}

	public void setEngineProfile(String engineProfile) {
		this.engineProfile = engineProfile;
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
}
