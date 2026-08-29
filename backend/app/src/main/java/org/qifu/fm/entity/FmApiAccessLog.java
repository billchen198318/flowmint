package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;

public class FmApiAccessLog implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String accessId;
	private String clientId;
	private String keyId;
	private String requestId;
	private String traceId;
	private String endpointCode;
	private String requiredScope;
	private String sourceIp;
	private String userAgentSummary;
	private Integer httpStatus;
	private String resultCode;
	private Long elapsedMillis;
	private Long requestBytes;
	private Long responseBytes;
	private String idempotencyKeyHash;
	private String processInstanceId;
	private String businessKey;
	private Date requestDate;
	private String cuserid;
	private Date cdate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String value) { oid = value; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String value) { tenantId = value; }
	public String getAccessId() { return accessId; }
	public void setAccessId(String value) { accessId = value; }
	public String getClientId() { return clientId; }
	public void setClientId(String value) { clientId = value; }
	public String getKeyId() { return keyId; }
	public void setKeyId(String value) { keyId = value; }
	public String getRequestId() { return requestId; }
	public void setRequestId(String value) { requestId = value; }
	public String getTraceId() { return traceId; }
	public void setTraceId(String value) { traceId = value; }
	public String getEndpointCode() { return endpointCode; }
	public void setEndpointCode(String value) { endpointCode = value; }
	public String getRequiredScope() { return requiredScope; }
	public void setRequiredScope(String value) { requiredScope = value; }
	public String getSourceIp() { return sourceIp; }
	public void setSourceIp(String value) { sourceIp = value; }
	public String getUserAgentSummary() { return userAgentSummary; }
	public void setUserAgentSummary(String value) { userAgentSummary = value; }
	public Integer getHttpStatus() { return httpStatus; }
	public void setHttpStatus(Integer value) { httpStatus = value; }
	public String getResultCode() { return resultCode; }
	public void setResultCode(String value) { resultCode = value; }
	public Long getElapsedMillis() { return elapsedMillis; }
	public void setElapsedMillis(Long value) { elapsedMillis = value; }
	public Long getRequestBytes() { return requestBytes; }
	public void setRequestBytes(Long value) { requestBytes = value; }
	public Long getResponseBytes() { return responseBytes; }
	public void setResponseBytes(Long value) { responseBytes = value; }
	public String getIdempotencyKeyHash() { return idempotencyKeyHash; }
	public void setIdempotencyKeyHash(String value) { idempotencyKeyHash = value; }
	public String getProcessInstanceId() { return processInstanceId; }
	public void setProcessInstanceId(String value) { processInstanceId = value; }
	public String getBusinessKey() { return businessKey; }
	public void setBusinessKey(String value) { businessKey = value; }
	public Date getRequestDate() { return requestDate; }
	public void setRequestDate(Date value) { requestDate = value; }
	@CreateUserField(name = "cuserid")
	public String getCuserid() { return cuserid; }
	public void setCuserid(String value) { cuserid = value; }
	@CreateDateField(name = "cdate")
	public Date getCdate() { return cdate; }
	public void setCdate(Date value) { cdate = value; }
}
