package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;
import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmOrgTitle implements Serializable {
  private static final long serialVersionUID = 1L;
  private String oid;
  private String tenantId;
  private String titleId;
  private String titleCode;
  private String titleName;
  private String approvalLevelId;
  private String isManagerTitle;
  private Integer sortNo;
  private String status;
  private Date effectiveFrom;
  private Date effectiveTo;
  private String description;
  private String cuserid;
  private Date cdate;
  private String uuserid;
  private Date udate;

  @EntityPK(name = "oid", autoUUID = true)
  public String getOid() { return oid; }
  public void setOid(String value) { oid = value; }
  public String getTenantId() { return tenantId; }
  public void setTenantId(String value) { tenantId = value; }
  public String getTitleId() { return titleId; }
  public void setTitleId(String value) { titleId = value; }
  public String getTitleCode() { return titleCode; }
  public void setTitleCode(String value) { titleCode = value; }
  public String getTitleName() { return titleName; }
  public void setTitleName(String value) { titleName = value; }
  public String getApprovalLevelId() { return approvalLevelId; }
  public void setApprovalLevelId(String value) { approvalLevelId = value; }
  public String getIsManagerTitle() { return isManagerTitle; }
  public void setIsManagerTitle(String value) { isManagerTitle = value; }
  public Integer getSortNo() { return sortNo; }
  public void setSortNo(Integer value) { sortNo = value; }
  public String getStatus() { return status; }
  public void setStatus(String value) { status = value; }
  public Date getEffectiveFrom() { return effectiveFrom; }
  public void setEffectiveFrom(Date value) { effectiveFrom = value; }
  public Date getEffectiveTo() { return effectiveTo; }
  public void setEffectiveTo(Date value) { effectiveTo = value; }
  public String getDescription() { return description; }
  public void setDescription(String value) { description = value; }
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
