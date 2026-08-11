package org.qifu.fm.domain.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmNotificationService;

class FmNotificationPublisherTest {

	@Test
	void createsOneTenantScopedNotificationPerDistinctRecipient() throws Exception {
		IFmNotificationService service = mock(IFmNotificationService.class);
		when(service.insertIfAbsent(any())).thenReturn(true);
		FmNotificationMailOutbox mailOutbox = mock(FmNotificationMailOutbox.class);
		FmNotificationPublisher publisher = publisher(service, mailOutbox);

		int inserted = publisher.taskAssigned(
				"T001", "TASK-1", "財務簽核",
				java.util.List.of("alice", "alice", "bob"), "starter", new Date(1000));

		assertEquals(2, inserted);
		ArgumentCaptor<FmNotification> captor = ArgumentCaptor.forClass(FmNotification.class);
		verify(service, times(2)).insertIfAbsent(captor.capture());
		FmNotification first = captor.getAllValues().getFirst();
		assertEquals("T001", first.getTenantId());
		assertEquals("TASK_ASSIGNED", first.getEventType());
		assertEquals("TASK", first.getReferenceType());
		assertEquals("TASK-1", first.getReferenceId());
		assertEquals("SENT", first.getDeliveryStatus());
		assertNotNull(first.getNotificationId());
		verify(mailOutbox, times(2)).enqueue(any());
	}

	@Test
	void usesStableEventIdentitySoRetriesCanBeIgnored() throws Exception {
		IFmNotificationService service = mock(IFmNotificationService.class);
		when(service.insertIfAbsent(any())).thenReturn(true, false);
		FmNotificationPublisher publisher = publisher(
				service, mock(FmNotificationMailOutbox.class));

		publisher.taskAssigned("T001", "TASK-1", "簽核",
				java.util.List.of("alice"), "starter", new Date(1000));
		publisher.taskAssigned("T001", "TASK-1", "簽核",
				java.util.List.of("alice"), "starter", new Date(2000));

		ArgumentCaptor<FmNotification> captor = ArgumentCaptor.forClass(FmNotification.class);
		verify(service, times(2)).insertIfAbsent(captor.capture());
		assertEquals(captor.getAllValues().get(0).getNotificationId(),
				captor.getAllValues().get(1).getNotificationId());
	}

	@Test
	void createsTerminalProcessNotificationForOwnerAndStarter() throws Exception {
		IFmNotificationService service = mock(IFmNotificationService.class);
		when(service.insertIfAbsent(any())).thenReturn(true);
		FmNotificationMailOutbox mailOutbox = mock(FmNotificationMailOutbox.class);
		FmNotificationPublisher publisher = publisher(service, mailOutbox);

		int inserted = publisher.processStatusChanged(
				"T001", "PROCESS-1", "COMPLETED",
				java.util.List.of("owner", "starter"), "approver", new Date());

		assertEquals(2, inserted);
		ArgumentCaptor<FmNotification> captor = ArgumentCaptor.forClass(FmNotification.class);
		verify(service, times(2)).insertIfAbsent(captor.capture());
		assertEquals("PROCESS_COMPLETED", captor.getValue().getEventType());
		assertEquals("PROCESS_INSTANCE", captor.getValue().getReferenceType());
		verify(mailOutbox, times(2)).enqueue(any());
	}

	@Test
	void createsDistinctDueSoonAndOverdueEventsOnlyOnce() throws Exception {
		IFmNotificationService service = mock(IFmNotificationService.class);
		when(service.insertIfAbsent(any())).thenReturn(true, false, true);
		FmNotificationPublisher publisher = publisher(
				service, mock(FmNotificationMailOutbox.class));

		publisher.taskDeadline("T001", "TASK-1", "財務簽核", "TASK_DUE_SOON",
				java.util.List.of("alice"), new Date(1000));
		publisher.taskDeadline("T001", "TASK-1", "財務簽核", "TASK_DUE_SOON",
				java.util.List.of("alice"), new Date(2000));
		publisher.taskDeadline("T001", "TASK-1", "財務簽核", "TASK_OVERDUE",
				java.util.List.of("alice"), new Date(3000));

		ArgumentCaptor<FmNotification> captor = ArgumentCaptor.forClass(FmNotification.class);
		verify(service, times(3)).insertIfAbsent(captor.capture());
		assertEquals(captor.getAllValues().get(0).getNotificationId(),
				captor.getAllValues().get(1).getNotificationId());
		assertEquals("TASK_OVERDUE", captor.getAllValues().get(2).getEventType());
	}

	private FmNotificationPublisher publisher(
			IFmNotificationService service, FmNotificationMailOutbox mailOutbox) {
		FmNotificationTemplateCatalog templates = mock(FmNotificationTemplateCatalog.class);
		when(templates.render(any(), any(), any())).thenReturn(
				new FmNotificationTemplateCatalog.TemplateText("通知", "內容"));
		return new FmNotificationPublisher(service, mailOutbox, templates);
	}
}
