package org.qifu.fm.dto.view;

import java.util.Date;

public record FmNotificationView(
		String notificationId,
		String eventType,
		String subject,
		String contentText,
		String referenceType,
		String referenceId,
		String deliveryStatus,
		Date createdDate,
		Date readDate) {
}
