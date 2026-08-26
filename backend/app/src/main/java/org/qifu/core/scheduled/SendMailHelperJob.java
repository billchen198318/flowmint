/*
 * Copyright 2012-2016 bambooCORE, greenstep of copyright Chen Xin Nien
 * Licensed under the Apache License, Version 2.0.
 */
package org.qifu.core.scheduled;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.qifu.base.Constants;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.base.scheduled.BaseScheduledTasksProvide;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.model.MailContent;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.core.util.MailClientUtils;
import org.qifu.core.util.SystemSettingConfigureUtils;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmNotificationService;
import org.qifu.util.SimpleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;

/**
 * FlowMint application-level replacement for the QIFU4 core
 * {@code org.qifu.core.scheduled.SendMailHelperJob}.
 *
 * <p>This class intentionally has the same package and class name as the class
 * packaged in the QIFU4 core JAR. Spring Boot loads this app class from
 * {@code BOOT-INF/classes} before the dependency JAR. Do not rename or move it.
 * Whenever QIFU4 is upgraded, compare this replacement with the latest core
 * implementation and merge applicable fixes.</p>
 *
 * <p>此類別刻意取代底層 QIFU4 core JAR 的同名寄信排程，讓 FlowMint 使用
 * {@code fm_notification} 保存跨排程的寄送次數與永久失敗狀態，且不修改
 * {@code tb_sys_mail_helper} schema。</p>
 */
@Component
public class SendMailHelperJob extends BaseScheduledTasksProvide {

	private static final Logger LOGGER = LoggerFactory.getLogger(SendMailHelperJob.class);
	private static final int RETRY_LIMIT = 3;
	private static final long RETRY_DELAY_MILLIS = 3000L;
	private static final int ERROR_LIMIT = 1000;
	private static final String FIND_ELIGIBLE_MAIL_SQL = """
			SELECT m.OID, m.MAIL_ID, m.SUBJECT, m.TEXT, m.MAIL_FROM, m.MAIL_TO,
			       m.MAIL_CC, m.MAIL_BCC, m.SUCCESS_FLAG, m.SUCCESS_TIME,
			       m.RETAIN_FLAG, m.CUSERID, m.CDATE, m.UUSERID, m.UDATE
			  FROM tb_sys_mail_helper m
			 WHERE m.MAIL_ID LIKE :mailIdPrefix
			   AND m.SUCCESS_FLAG = 'N'
			   AND NOT EXISTS (
			       SELECT 1
			         FROM fm_notification n
			        WHERE n.CHANNEL_TYPE = 'EMAIL'
			          AND n.PROVIDER_MESSAGE_ID = m.MAIL_ID
			          AND (n.DELIVERY_STATUS <> 'PENDING'
			               OR (n.NEXT_RETRY_DATE IS NOT NULL
			                   AND n.NEXT_RETRY_DATE > NOW())))
			 ORDER BY m.CDATE
			 LIMIT 100
			""";

	private final ISysMailHelperService<TbSysMailHelper, String> mailHelperService;
	private final IFmNotificationService notificationService;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public SendMailHelperJob(
			ISysMailHelperService<TbSysMailHelper, String> mailHelperService,
			IFmNotificationService notificationService,
			@Qualifier("db1JdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
		this.mailHelperService = mailHelperService;
		this.notificationService = notificationService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Scheduled(initialDelay = 5000, fixedDelay = 180000)
	public void execute() {
		try {
			login();
			if (!YesNoKeyProvide.YES.equals(
					SystemSettingConfigureUtils.getMailEnableValue())) {
				LOGGER.warn("Mail sender is disabled by CNF/CNF_CONF002");
				return;
			}
			String mailIdPrefix = SimpleUtils.getStrYMD("").substring(0, 6) + "%";
			for (TbSysMailHelper mail : findEligibleMail(mailIdPrefix)) {
				process(mail);
			}
		} catch (ServiceException exception) {
			LOGGER.error("Mail sender job failed", exception);
		} finally {
			logout();
		}
	}

	private List<TbSysMailHelper> findEligibleMail(String mailIdPrefix) {
		return jdbcTemplate.query(FIND_ELIGIBLE_MAIL_SQL,
				java.util.Map.of("mailIdPrefix", mailIdPrefix), (resultSet, rowNum) -> {
					TbSysMailHelper mail = new TbSysMailHelper();
					mail.setOid(resultSet.getString("OID"));
					mail.setMailId(resultSet.getString("MAIL_ID"));
					mail.setSubject(resultSet.getString("SUBJECT"));
					mail.setText(resultSet.getBytes("TEXT"));
					mail.setMailFrom(resultSet.getString("MAIL_FROM"));
					mail.setMailTo(resultSet.getString("MAIL_TO"));
					mail.setMailCc(resultSet.getString("MAIL_CC"));
					mail.setMailBcc(resultSet.getString("MAIL_BCC"));
					mail.setSuccessFlag(resultSet.getString("SUCCESS_FLAG"));
					mail.setSuccessTime(resultSet.getTimestamp("SUCCESS_TIME"));
					mail.setRetainFlag(resultSet.getString("RETAIN_FLAG"));
					mail.setCuserid(resultSet.getString("CUSERID"));
					mail.setCdate(resultSet.getTimestamp("CDATE"));
					mail.setUuserid(resultSet.getString("UUSERID"));
					mail.setUdate(resultSet.getTimestamp("UDATE"));
					return mail;
				});
	}

	void process(TbSysMailHelper mail) {
		if (mail == null) {
			return;
		}
		FmNotification notification =
				notificationService.findEmailByProviderMessageId(mail.getMailId());
		if (notification != null && !"PENDING".equals(notification.getDeliveryStatus())) {
			return;
		}
		int attempts = notification == null || notification.getRetryCount() == null
				? 0 : notification.getRetryCount();
		while (attempts < RETRY_LIMIT) {
			try {
				send(mail);
				handleSuccess(mail, notification);
				return;
			} catch (MailException | UnsupportedEncodingException
					| MessagingException exception) {
				attempts++;
				handleFailure(notification, mail.getMailId(), exception);
				LOGGER.error("Email delivery failed for mail-id {}. Attempt {} of {}",
						mail.getMailId(), attempts, RETRY_LIMIT, exception);
				if (attempts < RETRY_LIMIT && !pause()) {
					return;
				}
			}
		}
	}

	void send(TbSysMailHelper mail)
			throws MailException, UnsupportedEncodingException, MessagingException {
		MailClientUtils.send(mail.getMailFrom(), mail.getMailTo(), mail.getMailCc(),
				mail.getMailBcc(), new MailContent(mail.getSubject(),
						new String(mail.getText(), Constants.BASE_ENCODING)));
	}

	private void handleSuccess(TbSysMailHelper mail, FmNotification notification) {
		Date sentDate = new Date();
		try {
			if (YesNoKeyProvide.YES.equals(mail.getRetainFlag())) {
				mail.setSuccessFlag(YesNoKeyProvide.YES);
				mail.setSuccessTime(sentDate);
				mailHelperService.update(mail);
			} else {
				mailHelperService.delete(mail);
			}
			if (notification != null) {
				notificationService.markDelivered(notification.getTenantId(),
						notification.getNotificationId(), mail.getMailId(), sentDate);
			}
		} catch (ServiceException exception) {
			LOGGER.error("Cannot persist successful mail-id {}", mail.getMailId(), exception);
		}
	}

	private void handleFailure(FmNotification notification, String mailId,
			Exception exception) {
		if (notification == null) {
			return;
		}
		Date failedDate = new Date();
		Date nextRetryDate = new Date(failedDate.getTime() + RETRY_DELAY_MILLIS);
		notificationService.markDeliveryAttemptFailed(notification.getTenantId(),
				notification.getNotificationId(), mailId, failedDate, nextRetryDate,
				RETRY_LIMIT, abbreviate(exception));
	}

	boolean pause() {
		try {
			Thread.sleep(RETRY_DELAY_MILLIS);
			return true;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			LOGGER.warn("Mail retry interrupted", exception);
			return false;
		}
	}

	private String abbreviate(Exception exception) {
		String message = exception.getClass().getSimpleName() + ": "
				+ (exception.getMessage() == null ? "mail delivery failed"
						: exception.getMessage());
		return message.length() <= ERROR_LIMIT
				? message : message.substring(0, ERROR_LIMIT);
	}
}
