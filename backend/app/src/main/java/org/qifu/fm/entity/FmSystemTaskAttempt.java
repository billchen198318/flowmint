package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.EntityPK;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.CreateDateField;

public class FmSystemTaskAttempt implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String executionOid;
	private Integer attemptNo;
	private Integer generation;
	private String requestId;
	private String engineProfile;
	private Date startedAt;
	private Date leaseUntil;
	private String status;
	private Date completedAt;
	private Long durationMs;
	private String errorCode;
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

	public String getExecutionOid() {
		return executionOid;
	}

	public void setExecutionOid(String executionOid) {
		this.executionOid = executionOid;
	}

	public Integer getAttemptNo() {
		return attemptNo;
	}

	public void setAttemptNo(Integer attemptNo) {
		this.attemptNo = attemptNo;
	}

	public Integer getGeneration() {
		return generation;
	}

	public void setGeneration(Integer generation) {
		this.generation = generation;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getEngineProfile() {
		return engineProfile;
	}

	public void setEngineProfile(String engineProfile) {
		this.engineProfile = engineProfile;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getLeaseUntil() {
		return leaseUntil;
	}

	public void setLeaseUntil(Date leaseUntil) {
		this.leaseUntil = leaseUntil;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Date completedAt) {
		this.completedAt = completedAt;
	}

	public Long getDurationMs() {
		return durationMs;
	}

	public void setDurationMs(Long durationMs) {
		this.durationMs = durationMs;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
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
