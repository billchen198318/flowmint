package org.qifu.fm.domain.notification;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.fm.service.IFmNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FmNotificationDeliverySyncJob {

	private static final Logger LOGGER = LoggerFactory.getLogger(
			FmNotificationDeliverySyncJob.class);
	private static final int BATCH_SIZE = 200;

	private final IFmNotificationService notificationService;
	private final ISysMailHelperService<TbSysMailHelper, String> mailHelperService;

	public FmNotificationDeliverySyncJob(
			IFmNotificationService notificationService,
			ISysMailHelperService<TbSysMailHelper, String> mailHelperService) {
		this.notificationService = notificationService;
		this.mailHelperService = mailHelperService;
	}

	@Scheduled(initialDelay = 60000, fixedDelay = 180000)
	public void synchronize() {
		try {
			notificationService.findPendingEmail(BATCH_SIZE).forEach(this::synchronizeOne);
		} catch (Exception exception) {
			LOGGER.error("FlowMint Email 寄送結果同步失敗", exception);
		}
	}

	private void synchronizeOne(org.qifu.fm.entity.FmNotification notification) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("mailId", notification.getProviderMessageId());
			TbSysMailHelper mail = mailHelperService.selectListByParams(parameters)
					.getValue().stream().findFirst().orElse(null);
			if (mail != null && YesNoKeyProvide.YES.equals(mail.getSuccessFlag())) {
				Date sentDate = mail.getSuccessTime() == null ? new Date() : mail.getSuccessTime();
				notificationService.markDelivered(
						notification.getTenantId(), notification.getNotificationId(),
						notification.getProviderMessageId(), sentDate);
			}
		} catch (ServiceException exception) {
			LOGGER.error("FlowMint Email {} 寄送結果同步失敗",
					notification.getNotificationId(), exception);
		}
	}
}
