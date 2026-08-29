package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmApiClientKey implements Serializable {
	private static final long serialVersionUID = 1L;
	private String oid;
	private String tenantId;
	private String clientId;
	private String keyId;
	private String keyPrefix;
	private String keyLastFour;
	private String secretHash;
	private Date effectiveFrom;
	private Date expiresAt;
	private Date revokedAt;
	private String revokedBy;
	private String revokeReason;
	private Date lastUsedAt;
	private String lastSourceIp;
	private Integer failedCount;
	private String status;
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
	public String getClientId() { return clientId; }
	public void setClientId(String value) { clientId = value; }
	public String getKeyId() { return keyId; }
	public void setKeyId(String value) { keyId = value; }
	public String getKeyPrefix() { return keyPrefix; }
	public void setKeyPrefix(String value) { keyPrefix = value; }
	public String getKeyLastFour() { return keyLastFour; }
	public void setKeyLastFour(String value) { keyLastFour = value; }
	public String getSecretHash() { return secretHash; }
	public void setSecretHash(String value) { secretHash = value; }
	public Date getEffectiveFrom() { return effectiveFrom; }
	public void setEffectiveFrom(Date value) { effectiveFrom = value; }
	public Date getExpiresAt() { return expiresAt; }
	public void setExpiresAt(Date value) { expiresAt = value; }
	public Date getRevokedAt() { return revokedAt; }
	public void setRevokedAt(Date value) { revokedAt = value; }
	public String getRevokedBy() { return revokedBy; }
	public void setRevokedBy(String value) { revokedBy = value; }
	public String getRevokeReason() { return revokeReason; }
	public void setRevokeReason(String value) { revokeReason = value; }
	public Date getLastUsedAt() { return lastUsedAt; }
	public void setLastUsedAt(Date value) { lastUsedAt = value; }
	public String getLastSourceIp() { return lastSourceIp; }
	public void setLastSourceIp(String value) { lastSourceIp = value; }
	public Integer getFailedCount() { return failedCount; }
	public void setFailedCount(Integer value) { failedCount = value; }
	public String getStatus() { return status; }
	public void setStatus(String value) { status = value; }
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
