package org.qifu.fm.model;

import java.util.Date;

public class FmUserProcessHistoryRow {
    private String processInstanceId;
    private String businessKey;
    private String documentNumber;
    private String processName;
    private String formName;
    private String applicantAccount;
    private String starterAccount;
    private Boolean applicant;
    private Boolean starter;
    private Boolean approver;
    private String lastActionType;
    private String lastOutcome;
    private Date lastActionDate;
    private String instanceStatus;
    private Date startDate;
    private Date endDate;

    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String value) { this.processInstanceId = value; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String value) { this.businessKey = value; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String value) { this.documentNumber = value; }
    public String getProcessName() { return processName; }
    public void setProcessName(String value) { this.processName = value; }
    public String getFormName() { return formName; }
    public void setFormName(String value) { this.formName = value; }
    public String getApplicantAccount() { return applicantAccount; }
    public void setApplicantAccount(String value) { this.applicantAccount = value; }
    public String getStarterAccount() { return starterAccount; }
    public void setStarterAccount(String value) { this.starterAccount = value; }
    public Boolean getApplicant() { return applicant; }
    public void setApplicant(Boolean value) { this.applicant = value; }
    public Boolean getStarter() { return starter; }
    public void setStarter(Boolean value) { this.starter = value; }
    public Boolean getApprover() { return approver; }
    public void setApprover(Boolean value) { this.approver = value; }
    public String getLastActionType() { return lastActionType; }
    public void setLastActionType(String value) { this.lastActionType = value; }
    public String getLastOutcome() { return lastOutcome; }
    public void setLastOutcome(String value) { this.lastOutcome = value; }
    public Date getLastActionDate() { return lastActionDate; }
    public void setLastActionDate(Date value) { this.lastActionDate = value; }
    public String getInstanceStatus() { return instanceStatus; }
    public void setInstanceStatus(String value) { this.instanceStatus = value; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date value) { this.startDate = value; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date value) { this.endDate = value; }
}
