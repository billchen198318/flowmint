package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmApiClient implements Serializable {
	private static final long serialVersionUID = 1L;
	private String oid;
	private String tenantId;
	private String clientId;
	private String clientCode;
	private String clientName;
	private String systemType;
	private String description;
	private String allowedScopes;
	private String allowedProcessIds;
	private String allowedInitiatorAccounts;
	private String ipAllowlist;
	private Integer rateLimitPerMinute;
	private Integer dailyQuota;
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
	public String getClientCode() { return clientCode; }
	public void setClientCode(String value) { clientCode = value; }
	public String getClientName() { return clientName; }
	public void setClientName(String value) { clientName = value; }
	public String getSystemType() { return systemType; }
	public void setSystemType(String value) { systemType = value; }
	public String getDescription() { return description; }
	public void setDescription(String value) { description = value; }
	public String getAllowedScopes() { return allowedScopes; }
	public void setAllowedScopes(String value) { allowedScopes = value; }
	public String getAllowedProcessIds() { return allowedProcessIds; }
	public void setAllowedProcessIds(String value) { allowedProcessIds = value; }
	public String getAllowedInitiatorAccounts() { return allowedInitiatorAccounts; }
	public void setAllowedInitiatorAccounts(String value) { allowedInitiatorAccounts = value; }
	public String getIpAllowlist() { return ipAllowlist; }
	public void setIpAllowlist(String value) { ipAllowlist = value; }
	public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
	public void setRateLimitPerMinute(Integer value) { rateLimitPerMinute = value; }
	public Integer getDailyQuota() { return dailyQuota; }
	public void setDailyQuota(Integer value) { dailyQuota = value; }
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
