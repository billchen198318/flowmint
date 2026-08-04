package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmDataSourcePool implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String poolId;
	private String poolCode;
	private String poolName;
	private String dbType;
	private String driverClass;
	private String jdbcUrl;
	private String username;
	private String passwordContent;
	private Integer maximumPoolSize;
	private Integer minimumIdle;
	private Long connectionTimeoutMs;
	private Long idleTimeoutMs;
	private Long maxLifetimeMs;
	private String validationQuery;
	private String status;
	private Integer lockVersion;
	private String description;
	private String cuserid;
	private Date cdate;
	private String uuserid;
	private Date udate;

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
	public String getPoolId() {
		return poolId;
	}
	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}
	public String getPoolCode() {
		return poolCode;
	}
	public void setPoolCode(String poolCode) {
		this.poolCode = poolCode;
	}
	public String getPoolName() {
		return poolName;
	}
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getDriverClass() {
		return driverClass;
	}
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPasswordContent() {
		return passwordContent;
	}
	public void setPasswordContent(String passwordContent) {
		this.passwordContent = passwordContent;
	}
	public Integer getMaximumPoolSize() {
		return maximumPoolSize;
	}
	public void setMaximumPoolSize(Integer maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}
	public Integer getMinimumIdle() {
		return minimumIdle;
	}
	public void setMinimumIdle(Integer minimumIdle) {
		this.minimumIdle = minimumIdle;
	}
	public Long getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}
	public void setConnectionTimeoutMs(Long connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}
	public Long getIdleTimeoutMs() {
		return idleTimeoutMs;
	}
	public void setIdleTimeoutMs(Long idleTimeoutMs) {
		this.idleTimeoutMs = idleTimeoutMs;
	}
	public Long getMaxLifetimeMs() {
		return maxLifetimeMs;
	}
	public void setMaxLifetimeMs(Long maxLifetimeMs) {
		this.maxLifetimeMs = maxLifetimeMs;
	}
	public String getValidationQuery() {
		return validationQuery;
	}
	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getLockVersion() {
		return lockVersion;
	}
	public void setLockVersion(Integer lockVersion) {
		this.lockVersion = lockVersion;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	@UpdateUserField(name = "uuserid")
	public String getUuserid() {
		return uuserid;
	}
	public void setUuserid(String uuserid) {
		this.uuserid = uuserid;
	}
	@UpdateDateField(name = "udate")
	public Date getUdate() {
		return udate;
	}
	public void setUdate(Date udate) {
		this.udate = udate;
	}
}
