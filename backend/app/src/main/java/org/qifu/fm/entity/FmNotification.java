package org.qifu.fm.entity;

import java.io.Serializable;
import java.util.Date;

import org.qifu.base.model.CreateDateField;
import org.qifu.base.model.CreateUserField;
import org.qifu.base.model.EntityPK;
import org.qifu.base.model.UpdateDateField;
import org.qifu.base.model.UpdateUserField;

public class FmNotification implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String tenantId;
	private String notificationId;
	private String recipientAccount;
	private String channelType;
	private String eventType;
	private String subject;
	private String contentText;
	private String referenceType;
	private String referenceId;
	private String deliveryStatus;
	private Integer retryCount;
	private Date nextRetryDate;
	private Date sentDate;
	private Date readDate;
	private String lastError;
	private String providerMessageId;
	private String cuserid;
	private Date cdate;
	private String uuserid;
	private Date udate;

	@EntityPK(name = "oid", autoUUID = true)
	public String getOid() { return oid; }
	public void setOid(String oid) { this.oid = oid; }
	public String getTenantId() { return tenantId; }
	public void setTenantId(String tenantId) { this.tenantId = tenantId; }
	public String getNotificationId() { return notificationId; }
	public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
	public String getRecipientAccount() { return recipientAccount; }
	public void setRecipientAccount(String recipientAccount) { this.recipientAccount = recipientAccount; }
	public String getChannelType() { return channelType; }
	public void setChannelType(String channelType) { this.channelType = channelType; }
	public String getEventType() { return eventType; }
	public void setEventType(String eventType) { this.eventType = eventType; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getContentText() { return contentText; }
	public void setContentText(String contentText) { this.contentText = contentText; }
	public String getReferenceType() { return referenceType; }
	public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
	public String getReferenceId() { return referenceId; }
	public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
	public String getDeliveryStatus() { return deliveryStatus; }
	public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
	public Integer getRetryCount() { return retryCount; }
	public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
	public Date getNextRetryDate() { return nextRetryDate; }
	public void setNextRetryDate(Date nextRetryDate) { this.nextRetryDate = nextRetryDate; }
	public Date getSentDate() { return sentDate; }
	public void setSentDate(Date sentDate) { this.sentDate = sentDate; }
	public Date getReadDate() { return readDate; }
	public void setReadDate(Date readDate) { this.readDate = readDate; }
	public String getLastError() { return lastError; }
	public void setLastError(String lastError) { this.lastError = lastError; }
	public String getProviderMessageId() { return providerMessageId; }
	public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
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
