package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmProcessStartProxy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String startProxyId;
    private String principalAccount;
    private String proxyAccount;
    private String scopeType;
    private String scopeRefId;
    private String status;
    private Date effectiveFrom;
    private Date effectiveTo;
    private String reason;
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

    public String getStartProxyId() {
        return startProxyId;
    }

    public void setStartProxyId(String startProxyId) {
        this.startProxyId = startProxyId;
    }

    public String getPrincipalAccount() {
        return principalAccount;
    }

    public void setPrincipalAccount(String principalAccount) {
        this.principalAccount = principalAccount;
    }

    public String getProxyAccount() {
        return proxyAccount;
    }

    public void setProxyAccount(String proxyAccount) {
        this.proxyAccount = proxyAccount;
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeRefId() {
        return scopeRefId;
    }

    public void setScopeRefId(String scopeRefId) {
        this.scopeRefId = scopeRefId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Date getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Date effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
