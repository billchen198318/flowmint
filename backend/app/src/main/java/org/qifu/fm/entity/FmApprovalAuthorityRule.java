package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmApprovalAuthorityRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String approvalAuthorityRuleId;
    private String approvalAuthorityId;
    private Integer ruleSeq;
    private String conditionConfig;
    private String targetType;
    private String targetRefId;
    private String resolverConfig;
    private String stopAfterApproval;
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

    public String getApprovalAuthorityRuleId() {
        return approvalAuthorityRuleId;
    }

    public void setApprovalAuthorityRuleId(String approvalAuthorityRuleId) {
        this.approvalAuthorityRuleId = approvalAuthorityRuleId;
    }

    public String getApprovalAuthorityId() {
        return approvalAuthorityId;
    }

    public void setApprovalAuthorityId(String approvalAuthorityId) {
        this.approvalAuthorityId = approvalAuthorityId;
    }

    public Integer getRuleSeq() {
        return ruleSeq;
    }

    public void setRuleSeq(Integer ruleSeq) {
        this.ruleSeq = ruleSeq;
    }

    public String getConditionConfig() {
        return conditionConfig;
    }

    public void setConditionConfig(String conditionConfig) {
        this.conditionConfig = conditionConfig;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetRefId() {
        return targetRefId;
    }

    public void setTargetRefId(String targetRefId) {
        this.targetRefId = targetRefId;
    }

    public String getResolverConfig() {
        return resolverConfig;
    }

    public void setResolverConfig(String resolverConfig) {
        this.resolverConfig = resolverConfig;
    }

    public String getStopAfterApproval() {
        return stopAfterApproval;
    }

    public void setStopAfterApproval(String stopAfterApproval) {
        this.stopAfterApproval = stopAfterApproval;
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
