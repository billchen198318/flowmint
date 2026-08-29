package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;

public class FmAiAnalysisAccess implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String accessId;
	private String analysisId;
	private String taskId;
	private String actorAccount;
	private String accessType;
	private String resultStatus;
	private String errorCode;
	private Date requestDate;
	private String cuserid;
	private Date cdate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String oid) { this.oid = oid; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	public String getAccessId() { return accessId; }
	public void setAccessId(String accessId) { this.accessId = accessId; }
	public String getAnalysisId() { return analysisId; }
	public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
	public String getTaskId() { return taskId; }
	public void setTaskId(String taskId) { this.taskId = taskId; }
	public String getActorAccount() { return actorAccount; }
	public void setActorAccount(String actorAccount) { this.actorAccount = actorAccount; }
	public String getAccessType() { return accessType; }
	public void setAccessType(String accessType) { this.accessType = accessType; }
	public String getResultStatus() { return resultStatus; }
	public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
	public String getErrorCode() { return errorCode; }
	public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
	public Date getRequestDate() { return requestDate; }
	public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }
	@CreateUserField(name = "cuserid")
	public String getCuserid() { return cuserid; }
	public void setCuserid(String cuserid) { this.cuserid = cuserid; }
	@CreateDateField(name = "cdate")
	public Date getCdate() { return cdate; }
	public void setCdate(Date cdate) { this.cdate = cdate; }
}
