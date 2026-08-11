package org.qifu.fm.domain.notification;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmNotificationService;

class FmNotificationDeliverySyncJobTest {

	@Test
	void marksPendingNotificationSentFromRetainedQifuMailResult() throws Exception {
		IFmNotificationService notifications = mock(IFmNotificationService.class);
		@SuppressWarnings("unchecked")
		ISysMailHelperService<TbSysMailHelper, String> mails =
				mock(ISysMailHelperService.class);
		FmNotification notification = new FmNotification();
		notification.setTenantId("T001");
		notification.setNotificationId("N001");
		notification.setProviderMessageId("20260811000000001");
		when(notifications.findPendingEmail(200)).thenReturn(List.of(notification));
		TbSysMailHelper mail = new TbSysMailHelper();
		mail.setSuccessFlag(YesNoKeyProvide.YES);
		Date sentDate = new Date(1000);
		mail.setSuccessTime(sentDate);
		DefaultResult<List<TbSysMailHelper>> mailResult = new DefaultResult<>();
		mailResult.setValue(List.of(mail));
		when(mails.selectListByParams(anyMap())).thenReturn(mailResult);

		new FmNotificationDeliverySyncJob(notifications, mails).synchronize();

		verify(notifications).markDelivered(
				"T001", "N001", "20260811000000001", sentDate);
	}
}
