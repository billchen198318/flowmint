package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmProcessCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    private String oid;
    private String tenantId;
    private String categoryCode;
    private String categoryLabel;
    private String iconCode;
    private Integer sortOrder;
    private String status;
    private String cuserid;
    private Date cdate;
    private String uuserid;
    private Date udate;

    @EntityPK(name = "oid", autoUUID = true)
    public String getOid() { return oid; }
    public void setOid(String oid) { this.oid = oid; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryLabel() { return categoryLabel; }
    public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }
    public String getIconCode() { return iconCode; }
    public void setIconCode(String iconCode) { this.iconCode = iconCode; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
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
