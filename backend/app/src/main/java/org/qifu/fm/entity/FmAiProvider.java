package org.qifu.fm.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmAiProvider implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String providerCode;
	private String providerType;
	private String displayName;
	private String baseUrl;
	private String modelId;
	private String apiKeyContent;
	private BigDecimal temperature;
	private Integer maxOutputTokens;
	private Integer timeoutSeconds;
	private String defaultFlag;
	private Integer configVersion;
	private String status;
	private String lastTestStatus;
	private Date lastTestDate;
	private Integer lockVersion;
	private String cuserid;
	private Date cdate;
	private String uuserid;
	private Date udate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String oid) { this.oid = oid; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	public String getProviderCode() { return providerCode; }
	public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
	public String getProviderType() { return providerType; }
	public void setProviderType(String providerType) { this.providerType = providerType; }
	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) { this.displayName = displayName; }
	public String getBaseUrl() { return baseUrl; }
	public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
	public String getModelId() { return modelId; }
	public void setModelId(String modelId) { this.modelId = modelId; }
	public String getApiKeyContent() { return apiKeyContent; }
	public void setApiKeyContent(String apiKeyContent) { this.apiKeyContent = apiKeyContent; }
	public BigDecimal getTemperature() { return temperature; }
	public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }
	public Integer getMaxOutputTokens() { return maxOutputTokens; }
	public void setMaxOutputTokens(Integer maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
	public Integer getTimeoutSeconds() { return timeoutSeconds; }
	public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
	public String getDefaultFlag() { return defaultFlag; }
	public void setDefaultFlag(String defaultFlag) { this.defaultFlag = defaultFlag; }
	public Integer getConfigVersion() { return configVersion; }
	public void setConfigVersion(Integer configVersion) { this.configVersion = configVersion; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getLastTestStatus() { return lastTestStatus; }
	public void setLastTestStatus(String lastTestStatus) { this.lastTestStatus = lastTestStatus; }
	public Date getLastTestDate() { return lastTestDate; }
	public void setLastTestDate(Date lastTestDate) { this.lastTestDate = lastTestDate; }
	public Integer getLockVersion() { return lockVersion; }
	public void setLockVersion(Integer lockVersion) { this.lockVersion = lockVersion; }
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
