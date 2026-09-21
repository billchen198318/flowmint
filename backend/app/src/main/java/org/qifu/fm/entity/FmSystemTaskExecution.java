package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.EntityPK;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.CreateDateField;

public class FmSystemTaskExecution implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String invocationId;
    private String parentInvocationId;
	private String processInstanceId;
	private String processDefId;
	private Integer versionNo;
	private String nodeId;
	private String flowableExecutionId;
	private String firstJobId;
	private String taskType;
	private String bindingSha256;
	private String manifestSha256;
	private Integer inputRevisionNo;
	private String formDataId;
	private Integer expectedFormLock;
	private String inputContent;
	private String contextContent;
	private String inputSha256;
	private Date startedAt;
	private String status;
	private Integer generation;
	private Integer attemptNo;
	private Date leaseUntil;
	private Date nextAttemptAt;
	private String resultSha256;
	private String formSnapshotOid;
	private String incidentOid;
	private String errorCode;
	private Date completedAt;
	private String cuserid;
	private Date cdate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

    public String getParentInvocationId() {
        return parentInvocationId;
    }

    public void setParentInvocationId(String parentInvocationId) {
        this.parentInvocationId = parentInvocationId;
    }

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getInvocationId() {
		return invocationId;
	}

	public void setInvocationId(String invocationId) {
		this.invocationId = invocationId;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
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

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getFlowableExecutionId() {
		return flowableExecutionId;
	}

	public void setFlowableExecutionId(String flowableExecutionId) {
		this.flowableExecutionId = flowableExecutionId;
	}

	public String getFirstJobId() {
		return firstJobId;
	}

	public void setFirstJobId(String firstJobId) {
		this.firstJobId = firstJobId;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getBindingSha256() {
		return bindingSha256;
	}

	public void setBindingSha256(String bindingSha256) {
		this.bindingSha256 = bindingSha256;
	}

	public String getManifestSha256() {
		return manifestSha256;
	}

	public void setManifestSha256(String manifestSha256) {
		this.manifestSha256 = manifestSha256;
	}

	public Integer getInputRevisionNo() {
		return inputRevisionNo;
	}

	public void setInputRevisionNo(Integer inputRevisionNo) {
		this.inputRevisionNo = inputRevisionNo;
	}

	public String getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(String formDataId) {
		this.formDataId = formDataId;
	}

	public Integer getExpectedFormLock() {
		return expectedFormLock;
	}

	public void setExpectedFormLock(Integer expectedFormLock) {
		this.expectedFormLock = expectedFormLock;
	}

	public String getInputContent() {
		return inputContent;
	}

	public void setInputContent(String inputContent) {
		this.inputContent = inputContent;
	}

	public String getContextContent() {
		return contextContent;
	}

	public void setContextContent(String contextContent) {
		this.contextContent = contextContent;
	}

	public String getInputSha256() {
		return inputSha256;
	}

	public void setInputSha256(String inputSha256) {
		this.inputSha256 = inputSha256;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getGeneration() {
		return generation;
	}

	public void setGeneration(Integer generation) {
		this.generation = generation;
	}

	public Integer getAttemptNo() {
		return attemptNo;
	}

	public void setAttemptNo(Integer attemptNo) {
		this.attemptNo = attemptNo;
	}

	public Date getLeaseUntil() {
		return leaseUntil;
	}

	public void setLeaseUntil(Date leaseUntil) {
		this.leaseUntil = leaseUntil;
	}

	public Date getNextAttemptAt() {
		return nextAttemptAt;
	}

	public void setNextAttemptAt(Date nextAttemptAt) {
		this.nextAttemptAt = nextAttemptAt;
	}

	public String getResultSha256() {
		return resultSha256;
	}

	public void setResultSha256(String resultSha256) {
		this.resultSha256 = resultSha256;
	}

	public String getFormSnapshotOid() {
		return formSnapshotOid;
	}

	public void setFormSnapshotOid(String formSnapshotOid) {
		this.formSnapshotOid = formSnapshotOid;
	}

	public String getIncidentOid() {
		return incidentOid;
	}

	public void setIncidentOid(String incidentOid) {
		this.incidentOid = incidentOid;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Date getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Date completedAt) {
		this.completedAt = completedAt;
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
