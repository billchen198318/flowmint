package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmDocumentNumberRule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String documentType;
    private String prefix;
    private String periodType;
    private Integer sequenceLength;
    private String formatPattern;
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
    public String getDocumentType() {
        return documentType;
    }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getPeriodType() {
        return periodType;
    }
    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }
    public Integer getSequenceLength() {
        return sequenceLength;
    }
    public void setSequenceLength(Integer sequenceLength) {
        this.sequenceLength = sequenceLength;
    }
    public String getFormatPattern() {
        return formatPattern;
    }
    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
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
