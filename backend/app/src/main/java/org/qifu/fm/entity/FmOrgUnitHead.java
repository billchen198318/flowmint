package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmOrgUnitHead implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String orgUnitHeadId;
	private String orgUnitId;
	private String employeeId;
	private String headType;
	private Integer priority;
	private String status;
	private Date effectiveFrom;
	private Date effectiveTo;
	private String description;
	private String cuserid;
	private Date cdate;
	private String uuserid;
	private Date udate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String oid) { this.oid = oid; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	public String getOrgUnitHeadId() { return orgUnitHeadId; }
	public void setOrgUnitHeadId(String orgUnitHeadId) { this.orgUnitHeadId = orgUnitHeadId; }
	public String getOrgUnitId() { return orgUnitId; }
	public void setOrgUnitId(String orgUnitId) { this.orgUnitId = orgUnitId; }
	public String getEmployeeId() { return employeeId; }
	public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
	public String getHeadType() { return headType; }
	public void setHeadType(String headType) { this.headType = headType; }
	public Integer getPriority() { return priority; }
	public void setPriority(Integer priority) { this.priority = priority; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Date getEffectiveFrom() { return effectiveFrom; }
	public void setEffectiveFrom(Date effectiveFrom) { this.effectiveFrom = effectiveFrom; }
	public Date getEffectiveTo() { return effectiveTo; }
	public void setEffectiveTo(Date effectiveTo) { this.effectiveTo = effectiveTo; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
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
