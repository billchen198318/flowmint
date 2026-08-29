package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmApiRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String oid;
	private String tenantId;
	private String apiRequestId;
	private String clientId;
	private String keyId;
	private String idempotencyKeyHash;
	private String payloadHash;
	private String processDefId;
	private Integer processVersionNo;
	private String formId;
	private Integer formVersionNo;
	private String formSchemaHash;
	private String initiatorAccount;
	private String applicantAccount;
	private String applicantOrgUnitId;
	private String sourceSystem;
	private String sourceDocumentType;
	private String sourceDocumentNo;
	private String processInstanceId;
	private String flowableProcessInstanceId;
	private String businessKey;
	private String documentNumber;
	private String formDataId;
	private String requestStatus;
	private String resultCode;
	private String safeErrorMessage;
	private Date completedAt;
	private Integer lockVersion;
	private String cuserid;
	private Date cdate;
	private String uuserid;
	private Date udate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String value) { oid = value; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String value) { tenantId = value; }
	public String getApiRequestId() { return apiRequestId; }
	public void setApiRequestId(String value) { apiRequestId = value; }
	public String getClientId() { return clientId; }
	public void setClientId(String value) { clientId = value; }
	public String getKeyId() { return keyId; }
	public void setKeyId(String value) { keyId = value; }
	public String getIdempotencyKeyHash() { return idempotencyKeyHash; }
	public void setIdempotencyKeyHash(String value) { idempotencyKeyHash = value; }
	public String getPayloadHash() { return payloadHash; }
	public void setPayloadHash(String value) { payloadHash = value; }
	public String getProcessDefId() { return processDefId; }
	public void setProcessDefId(String value) { processDefId = value; }
	public Integer getProcessVersionNo() { return processVersionNo; }
	public void setProcessVersionNo(Integer value) { processVersionNo = value; }
	public String getFormId() { return formId; }
	public void setFormId(String value) { formId = value; }
	public Integer getFormVersionNo() { return formVersionNo; }
	public void setFormVersionNo(Integer value) { formVersionNo = value; }
	public String getFormSchemaHash() { return formSchemaHash; }
	public void setFormSchemaHash(String value) { formSchemaHash = value; }
	public String getInitiatorAccount() { return initiatorAccount; }
	public void setInitiatorAccount(String value) { initiatorAccount = value; }
	public String getApplicantAccount() { return applicantAccount; }
	public void setApplicantAccount(String value) { applicantAccount = value; }
	public String getApplicantOrgUnitId() { return applicantOrgUnitId; }
	public void setApplicantOrgUnitId(String value) { applicantOrgUnitId = value; }
	public String getSourceSystem() { return sourceSystem; }
	public void setSourceSystem(String value) { sourceSystem = value; }
	public String getSourceDocumentType() { return sourceDocumentType; }
	public void setSourceDocumentType(String value) { sourceDocumentType = value; }
	public String getSourceDocumentNo() { return sourceDocumentNo; }
	public void setSourceDocumentNo(String value) { sourceDocumentNo = value; }
	public String getProcessInstanceId() { return processInstanceId; }
	public void setProcessInstanceId(String value) { processInstanceId = value; }
	public String getFlowableProcessInstanceId() { return flowableProcessInstanceId; }
	public void setFlowableProcessInstanceId(String value) { flowableProcessInstanceId = value; }
	public String getBusinessKey() { return businessKey; }
	public void setBusinessKey(String value) { businessKey = value; }
	public String getDocumentNumber() { return documentNumber; }
	public void setDocumentNumber(String value) { documentNumber = value; }
	public String getFormDataId() { return formDataId; }
	public void setFormDataId(String value) { formDataId = value; }
	public String getRequestStatus() { return requestStatus; }
	public void setRequestStatus(String value) { requestStatus = value; }
	public String getResultCode() { return resultCode; }
	public void setResultCode(String value) { resultCode = value; }
	public String getSafeErrorMessage() { return safeErrorMessage; }
	public void setSafeErrorMessage(String value) { safeErrorMessage = value; }
	public Date getCompletedAt() { return completedAt; }
	public void setCompletedAt(Date value) { completedAt = value; }
	public Integer getLockVersion() { return lockVersion; }
	public void setLockVersion(Integer value) { lockVersion = value; }
	@CreateUserField(name = "cuserid")
	public String getCuserid() { return cuserid; }
	public void setCuserid(String value) { cuserid = value; }
	@CreateDateField(name = "cdate")
	public Date getCdate() { return cdate; }
	public void setCdate(Date value) { cdate = value; }
	@UpdateUserField(name = "uuserid")
	public String getUuserid() { return uuserid; }
	public void setUuserid(String value) { uuserid = value; }
	@UpdateDateField(name = "udate")
	public Date getUdate() { return udate; }
	public void setUdate(Date value) { udate = value; }
}
