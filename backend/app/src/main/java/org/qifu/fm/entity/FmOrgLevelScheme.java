package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;
import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmOrgLevelScheme implements Serializable {

  private static final long serialVersionUID = 1L;
  private String oid;
  private String tenantId;
  private String levelSchemeId;
  private String schemeCode;
  private String schemeName;
  private String isDefault;
  private String status;
  private String description;
  private Date effectiveFrom;
  private Date effectiveTo;
  private Date cdate;
  private Date udate;
  private String cuserid;
  private String uuserid;

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

  public String getLevelSchemeId() {
    return levelSchemeId;
  }

  public void setLevelSchemeId(String levelSchemeId) {
    this.levelSchemeId = levelSchemeId;
  }

  public String getSchemeCode() {
    return schemeCode;
  }

  public void setSchemeCode(String schemeCode) {
    this.schemeCode = schemeCode;
  }

  public String getSchemeName() {
    return schemeName;
  }

  public void setSchemeName(String schemeName) {
    this.schemeName = schemeName;
  }

  public String getIsDefault() {
    return isDefault;
  }

  public void setIsDefault(String isDefault) {
    this.isDefault = isDefault;
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
