package org.qifu.fm.service;

import java.util.Date;
import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmNotification;

public interface IFmNotificationService extends IBaseService<FmNotification, String> {

	List<FmNotification> findInbox(String tenantId, String recipientAccount, int limit);

	long countUnread(String tenantId, String recipientAccount);

	int markRead(String tenantId, String recipientAccount, String notificationId,
			Date readDate);

	int markAllRead(String tenantId, String recipientAccount, Date readDate);

	boolean insertIfAbsent(FmNotification notification);

	List<FmNotification> findPendingEmail(int limit);

	boolean markDelivered(String tenantId, String notificationId,
			String providerMessageId, Date sentDate);
}
