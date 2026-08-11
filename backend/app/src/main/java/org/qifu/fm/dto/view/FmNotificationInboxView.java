package org.qifu.fm.dto.view;

import java.util.List;

public record FmNotificationInboxView(long unreadCount, List<FmNotificationView> notifications) {
}
