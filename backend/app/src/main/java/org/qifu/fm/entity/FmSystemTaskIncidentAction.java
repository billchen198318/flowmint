package org.qifu.fm.entity;

import java.util.Date;

import org.qifu.base.model.EntityPK;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.CreateDateField;

public class FmSystemTaskIncidentAction implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String executionOid;
	private Integer actionNo;
	private String requestId;
	private String fromStatus;
	private String toStatus;
	private String reason;
	private String actor;
	private Date actionDate;
	private String cuserid;
	private Date cdate;
    private String actionType = "STATUS";
    private Integer expectedGeneration;
    private String targetInvocationId;
    private String requestSha256;

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

	public Integer getActionNo() {
		return actionNo;
	}

	public void setActionNo(Integer actionNo) {
		this.actionNo = actionNo;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(String fromStatus) {
		this.fromStatus = fromStatus;
	}

	public String getToStatus() {
		return toStatus;
	}

	public void setToStatus(String toStatus) {
		this.toStatus = toStatus;
	}

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getExpectedGeneration() {
        return expectedGeneration;
    }

    public void setExpectedGeneration(Integer expectedGeneration) {
        this.expectedGeneration = expectedGeneration;
    }

    public String getTargetInvocationId() {
        return targetInvocationId;
    }

    public void setTargetInvocationId(String targetInvocationId) {
        this.targetInvocationId = targetInvocationId;
    }

    public String getRequestSha256() {
        return requestSha256;
    }

    public void setRequestSha256(String requestSha256) {
        this.requestSha256 = requestSha256;
    }

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
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
