package org.qifu.fm.domain.notification;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.fm.entity.FmNotification;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.service.IFmNotificationService;
import org.springframework.stereotype.Component;

@Component
public class FmNotificationPublisher {

	private static final String TASK_ASSIGNED = "TASK_ASSIGNED";

	private final IFmNotificationService notificationService;
	private final FmNotificationMailOutbox mailOutbox;
	private final FmNotificationTemplateCatalog templateCatalog;

	public FmNotificationPublisher(IFmNotificationService notificationService,
			FmNotificationMailOutbox mailOutbox,
			FmNotificationTemplateCatalog templateCatalog) {
		this.notificationService = notificationService;
		this.mailOutbox = mailOutbox;
		this.templateCatalog = templateCatalog;
	}

	public int taskAssigned(String tenantId, String taskId, String taskName,
			Iterable<String> recipientAccounts, String actor, Date now)
			throws ServiceException {
		Set<String> recipients = distinctRecipients(recipientAccounts);
		int inserted = 0;
		for (String recipient : recipients) {
			FmNotification notification = notification(
					tenantId, taskId, taskName, recipient, actor, now);
			if (notificationService.insertIfAbsent(notification)) {
				inserted++;
			}
			mailOutbox.enqueue(notification);
		}
		return inserted;
	}

	public int processStatusChanged(String tenantId, String processInstanceId,
			String status, Iterable<String> recipientAccounts, String actor, Date now)
			throws ServiceException {
		String eventType = "PROCESS_" + status;
		int inserted = 0;
		for (String recipient : distinctRecipients(recipientAccounts)) {
			var template = templateCatalog.render(eventType, processInstanceId, null);
			FmNotification notification = new FmNotification();
			notification.setOid(UUID.randomUUID().toString());
			notification.setTenantId(tenantId);
			notification.setNotificationId(deterministicId(
					tenantId, eventType, processInstanceId, recipient));
			notification.setRecipientAccount(recipient);
			notification.setChannelType("IN_APP");
			notification.setEventType(eventType);
			notification.setSubject(template.subject());
			notification.setContentText(template.content());
			notification.setReferenceType("PROCESS_INSTANCE");
			notification.setReferenceId(processInstanceId);
			notification.setDeliveryStatus("SENT");
			notification.setRetryCount(0);
			notification.setSentDate(now);
			notification.setCuserid(actor);
			notification.setCdate(now);
			if (notificationService.insertIfAbsent(notification)) {
				inserted++;
			}
			mailOutbox.enqueue(notification);
		}
		return inserted;
	}

	public int taskDeadline(String tenantId, String taskId, String taskName,
			String eventType, Iterable<String> recipientAccounts, Date now)
			throws ServiceException {
		if (!Set.of("TASK_DUE_SOON", "TASK_OVERDUE").contains(eventType)) {
			throw new IllegalArgumentException("Unsupported task deadline event");
		}
		int inserted = 0;
		for (String recipient : distinctRecipients(recipientAccounts)) {
			var template = templateCatalog.render(eventType, taskId, taskName);
			FmNotification notification = new FmNotification();
			notification.setOid(UUID.randomUUID().toString());
			notification.setTenantId(tenantId);
			notification.setNotificationId(deterministicId(
					tenantId, eventType, taskId, recipient));
			notification.setRecipientAccount(recipient);
			notification.setChannelType("IN_APP");
			notification.setEventType(eventType);
			notification.setSubject(template.subject());
			notification.setContentText(template.content());
			notification.setReferenceType("TASK");
			notification.setReferenceId(taskId);
			notification.setDeliveryStatus("SENT");
			notification.setRetryCount(0);
			notification.setSentDate(now);
			notification.setCuserid("SYSTEM");
			notification.setCdate(now);
			if (notificationService.insertIfAbsent(notification)) {
				inserted++;
			}
			mailOutbox.enqueue(notification);
		}
		return inserted;
	}

	public int taskReassigned(String tenantId, String taskId, String taskName,
			String eventKey, String previousAssignee, String targetAssignee,
			String actor, Date now) throws ServiceException {
		int inserted = 0;
		inserted += taskEvent(tenantId, taskId, taskName, eventKey,
				"TASK_ADMIN_REASSIGNED_FROM", previousAssignee, actor, now);
		inserted += taskEvent(tenantId, taskId, taskName, eventKey,
				"TASK_ADMIN_REASSIGNED_TO", targetAssignee, actor, now);
		return inserted;
	}

	private int taskEvent(String tenantId, String taskId, String taskName,
			String eventKey, String eventType, String recipient, String actor, Date now)
			throws ServiceException {
		if (StringUtils.isBlank(recipient)) {
			return 0;
		}
		var template = templateCatalog.render(eventType, taskId, taskName);
		FmNotification notification = new FmNotification();
		notification.setOid(UUID.randomUUID().toString());
		notification.setTenantId(tenantId);
		notification.setNotificationId(deterministicId(
				tenantId, eventType, eventKey, recipient.trim()));
		notification.setRecipientAccount(recipient.trim());
		notification.setChannelType("IN_APP");
		notification.setEventType(eventType);
		notification.setSubject(template.subject());
		notification.setContentText(template.content());
		notification.setReferenceType("TASK");
		notification.setReferenceId(taskId);
		notification.setDeliveryStatus("SENT");
		notification.setRetryCount(0);
		notification.setSentDate(now);
		notification.setCuserid(actor);
		notification.setCdate(now);
		boolean inserted = notificationService.insertIfAbsent(notification);
		mailOutbox.enqueue(notification);
		return inserted ? 1 : 0;
	}

	public int parallelAddSignEvent(String tenantId, String referenceId, String eventKey,
			String eventType, Iterable<String> recipientAccounts,
			String actor, Date now) throws ServiceException {
		if (!Set.of("PARALLEL_ADD_SIGN_ASSIGNED", "PARALLEL_ADD_SIGN_REPLIED",
				"PARALLEL_ADD_SIGN_COMPLETED",
				"PARALLEL_ADD_SIGN_CANCELLED").contains(eventType)) {
			throw new IllegalArgumentException("Unsupported parallel add-sign event");
		}
		int inserted = 0;
		for (String recipient : distinctRecipients(recipientAccounts)) {
			var template = templateCatalog.render(eventType, referenceId, null);
			FmNotification notification = new FmNotification();
			notification.setOid(UUID.randomUUID().toString());
			notification.setTenantId(tenantId);
			notification.setNotificationId(deterministicId(
					tenantId, eventType, eventKey, recipient));
			notification.setRecipientAccount(recipient);
			notification.setChannelType("IN_APP");
			notification.setEventType(eventType);
			notification.setSubject(template.subject());
			notification.setContentText(template.content());
			notification.setReferenceType("TASK");
			notification.setReferenceId(referenceId);
			notification.setDeliveryStatus("SENT");
			notification.setRetryCount(0);
			notification.setSentDate(now);
			notification.setCuserid(actor);
			notification.setCdate(now);
			if (notificationService.insertIfAbsent(notification)) {
				inserted++;
			}
			mailOutbox.enqueue(notification);
		}
		return inserted;
	}

	private Set<String> distinctRecipients(Iterable<String> recipientAccounts) {
		Set<String> recipients = new LinkedHashSet<>();
		recipientAccounts.forEach(account -> {
			if (StringUtils.isNotBlank(account)) {
				recipients.add(account.trim());
			}
		});
		return recipients;
	}

	private FmNotification notification(String tenantId, String taskId,
			String taskName, String recipient, String actor, Date now) {
		var template = templateCatalog.render(TASK_ASSIGNED, taskId, taskName);
		FmNotification notification = new FmNotification();
		notification.setOid(UUID.randomUUID().toString());
		notification.setTenantId(tenantId);
		notification.setNotificationId(deterministicId(
				tenantId, TASK_ASSIGNED, taskId, recipient));
		notification.setRecipientAccount(recipient);
		notification.setChannelType("IN_APP");
		notification.setEventType(TASK_ASSIGNED);
		notification.setSubject(template.subject());
		notification.setContentText(template.content());
		notification.setReferenceType("TASK");
		notification.setReferenceId(taskId);
		notification.setDeliveryStatus("SENT");
		notification.setRetryCount(0);
		notification.setSentDate(now);
		notification.setCuserid(actor);
		notification.setCdate(now);
		return notification;
	}

	private String deterministicId(String tenantId, String eventType,
			String referenceId, String recipient) {
		String source = String.join(":", tenantId, eventType, referenceId, recipient);
		return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)).toString();
	}
}
