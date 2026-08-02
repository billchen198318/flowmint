package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmTaskFormRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String processDefId;
    private Integer processVersionNo;
    private String taskDefKey;
    private String formId;
    private Integer formVersionNo;
    private String fieldPolicy;
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

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public Integer getFormVersionNo() {
        return formVersionNo;
    }

    public void setFormVersionNo(Integer formVersionNo) {
        this.formVersionNo = formVersionNo;
    }

    public String getFieldPolicy() {
        return fieldPolicy;
    }

    public void setFieldPolicy(String fieldPolicy) {
        this.fieldPolicy = fieldPolicy;
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
