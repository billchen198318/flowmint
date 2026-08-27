package org.qifu.core.scheduled;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmNotificationService;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.MailException;

import jakarta.mail.MessagingException;

class SendMailHelperJobBehaviorTest {

	@Test
	void stopsAfterThirdFlowMintFailureAndPersistsEveryAttempt() {
		Fixture fixture = fixture("PENDING", 0);
		SendMailHelperJob job = fixture.failingJob();

		job.process(fixture.mail);

		verify(fixture.notificationService, times(3)).markDeliveryAttemptFailed(
				eq("A01"), eq("NOTIFY-1"), eq("MAIL-1"), any(), any(),
				eq(3), any());
		verify(fixture.notificationService, never()).markDelivered(
				any(), any(), any(), any());
	}

	@Test
	void doesNotSendNotificationAlreadyMarkedFailed() {
		Fixture fixture = fixture("FAILED", 3);
		SendMailHelperJob job = fixture.successJob();

		job.process(fixture.mail);

		verify(fixture.mailHelperService, never()).update(any());
		verify(fixture.notificationService, never()).markDelivered(
				any(), any(), any(), any());
	}

	@Test
	void successfulDeliveryUpdatesQifuAndFlowMint() throws Exception {
		Fixture fixture = fixture("PENDING", 1);
		SendMailHelperJob job = fixture.successJob();

		job.process(fixture.mail);

		verify(fixture.mailHelperService).update(fixture.mail);
		verify(fixture.notificationService).markDelivered(
				eq("A01"), eq("NOTIFY-1"), eq("MAIL-1"), any());
	}

	@SuppressWarnings("unchecked")
	private Fixture fixture(String status, int retryCount) {
		ISysMailHelperService<TbSysMailHelper, String> mailService =
				mock(ISysMailHelperService.class);
		IFmNotificationService notificationService =
				mock(IFmNotificationService.class);
		NamedParameterJdbcTemplate jdbcTemplate =
				mock(NamedParameterJdbcTemplate.class);
		TbSysMailHelper mail = new TbSysMailHelper();
		mail.setMailId("MAIL-1");
		mail.setRetainFlag("Y");
		FmNotification notification = new FmNotification();
		notification.setTenantId("A01");
		notification.setNotificationId("NOTIFY-1");
		notification.setProviderMessageId("MAIL-1");
		notification.setDeliveryStatus(status);
		notification.setRetryCount(retryCount);
		when(notificationService.findEmailByProviderMessageId("MAIL-1"))
				.thenReturn(notification);
		return new Fixture(mailService, notificationService, jdbcTemplate, mail);
	}

	private record Fixture(
			ISysMailHelperService<TbSysMailHelper, String> mailHelperService,
			IFmNotificationService notificationService,
			NamedParameterJdbcTemplate jdbcTemplate,
			TbSysMailHelper mail) {

		SendMailHelperJob failingJob() {
			return new SendMailHelperJob(mailHelperService, notificationService,
					jdbcTemplate) {
				@Override
				void send(TbSysMailHelper ignored)
						throws MailException, UnsupportedEncodingException,
						MessagingException {
					throw new MessagingException("smtp unavailable");
				}

				@Override
				boolean pause() {
					return true;
				}
			};
		}

		SendMailHelperJob successJob() {
			return new SendMailHelperJob(mailHelperService, notificationService,
					jdbcTemplate) {
				@Override
				void send(TbSysMailHelper ignored) {
					// Successful test transport.
				}
			};
		}
	}
}
