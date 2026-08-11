package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskAssignmentRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String processDefId;
    private Integer processVersionNo;
    private String taskDefKey;
    private Integer ruleSeq;
    private String resolverType;
    private String resolverConfig;
    private String fallbackConfig;
    private Integer maxResults;
    private String status;
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

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public Integer getProcessVersionNo() {
        return processVersionNo;
    }

    public void setProcessVersionNo(Integer processVersionNo) {
        this.processVersionNo = processVersionNo;
    }

    public String getTaskDefKey() {
        return taskDefKey;
    }

    public void setTaskDefKey(String taskDefKey) {
        this.taskDefKey = taskDefKey;
    }

    public Integer getRuleSeq() {
        return ruleSeq;
    }

    public void setRuleSeq(Integer ruleSeq) {
        this.ruleSeq = ruleSeq;
    }

    public String getResolverType() {
        return resolverType;
    }

    public void setResolverType(String resolverType) {
        this.resolverType = resolverType;
    }

    public String getResolverConfig() {
        return resolverConfig;
    }

    public void setResolverConfig(String resolverConfig) {
        this.resolverConfig = resolverConfig;
    }

    public String getFallbackConfig() {
        return fallbackConfig;
    }

    public void setFallbackConfig(String fallbackConfig) {
        this.fallbackConfig = fallbackConfig;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
