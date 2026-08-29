package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;

public class FmAiAnalysis implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String analysisId;
	private String processInstanceId;
	private String taskId;
	private String taskDefKey;
	private String formDataId;
	private Integer formRevision;
	private String actorAccount;
	private String providerCode;
	private String providerType;
	private String modelId;
	private Integer configVersion;
	private Integer promptTemplateVersion;
	private String contentHash;
	private Integer generationNo;
	private String forceRefreshFlag;
	private String executionStatus;
	private String errorCode;
	private Date startDate;
	private Date completeDate;
	private Long elapsedMillis;
	private Integer inputTokens;
	private Integer outputTokens;
	private String resultContent;
	private String cuserid;
	private Date cdate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String oid) { this.oid = oid; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	public String getAnalysisId() { return analysisId; }
	public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }
	public String getProcessInstanceId() { return processInstanceId; }
	public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
	public String getTaskId() { return taskId; }
	public void setTaskId(String taskId) { this.taskId = taskId; }
	public String getTaskDefKey() { return taskDefKey; }
	public void setTaskDefKey(String taskDefKey) { this.taskDefKey = taskDefKey; }
	public String getFormDataId() { return formDataId; }
	public void setFormDataId(String formDataId) { this.formDataId = formDataId; }
	public Integer getFormRevision() { return formRevision; }
	public void setFormRevision(Integer formRevision) { this.formRevision = formRevision; }
	public String getActorAccount() { return actorAccount; }
	public void setActorAccount(String actorAccount) { this.actorAccount = actorAccount; }
	public String getProviderCode() { return providerCode; }
	public void setProviderCode(String providerCode) { this.providerCode = providerCode; }
	public String getProviderType() { return providerType; }
	public void setProviderType(String providerType) { this.providerType = providerType; }
	public String getModelId() { return modelId; }
	public void setModelId(String modelId) { this.modelId = modelId; }
	public Integer getConfigVersion() { return configVersion; }
	public void setConfigVersion(Integer configVersion) { this.configVersion = configVersion; }
	public Integer getPromptTemplateVersion() { return promptTemplateVersion; }
	public void setPromptTemplateVersion(Integer promptTemplateVersion) { this.promptTemplateVersion = promptTemplateVersion; }
	public String getContentHash() { return contentHash; }
	public void setContentHash(String contentHash) { this.contentHash = contentHash; }
	public Integer getGenerationNo() { return generationNo; }
	public void setGenerationNo(Integer generationNo) { this.generationNo = generationNo; }
	public String getForceRefreshFlag() { return forceRefreshFlag; }
	public void setForceRefreshFlag(String forceRefreshFlag) { this.forceRefreshFlag = forceRefreshFlag; }
	public String getExecutionStatus() { return executionStatus; }
	public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
	public String getErrorCode() { return errorCode; }
	public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
	public Date getStartDate() { return startDate; }
	public void setStartDate(Date startDate) { this.startDate = startDate; }
	public Date getCompleteDate() { return completeDate; }
	public void setCompleteDate(Date completeDate) { this.completeDate = completeDate; }
	public Long getElapsedMillis() { return elapsedMillis; }
	public void setElapsedMillis(Long elapsedMillis) { this.elapsedMillis = elapsedMillis; }
	public Integer getInputTokens() { return inputTokens; }
	public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }
	public Integer getOutputTokens() { return outputTokens; }
	public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }
	public String getResultContent() { return resultContent; }
	public void setResultContent(String resultContent) { this.resultContent = resultContent; }
	@CreateUserField(name = "cuserid")
	public String getCuserid() { return cuserid; }
	public void setCuserid(String cuserid) { this.cuserid = cuserid; }
	@CreateDateField(name = "cdate")
	public Date getCdate() { return cdate; }
	public void setCdate(Date cdate) { this.cdate = cdate; }
}
