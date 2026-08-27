package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.mapper.FmNotificationMapper;
import org.qifu.fm.service.IFmNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmNotificationServiceImpl extends BaseService<FmNotification, String>
		implements IFmNotificationService {

	private final FmNotificationMapper mapper;

	public FmNotificationServiceImpl(FmNotificationMapper mapper) { this.mapper = mapper; }

	@Override
	protected IBaseMapper<FmNotification, String> getBaseMapper() { return mapper; }

	@Override
	public List<FmNotification> findInbox(String tenantId, String account, int limit) {
		Map<String, Object> parameters = identity(tenantId, account);
		parameters.put("limit", limit);
		return mapper.findInbox(parameters);
	}

	@Override
	public long countUnread(String tenantId, String account) {
		return mapper.countUnread(identity(tenantId, account));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int markRead(String tenantId, String account, String notificationId, Date readDate) {
		Map<String, Object> parameters = identity(tenantId, account);
		parameters.put("notificationId", notificationId);
		parameters.put("readDate", readDate);
		return mapper.markRead(parameters);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int markAllRead(String tenantId, String account, Date readDate) {
		Map<String, Object> parameters = identity(tenantId, account);
		parameters.put("readDate", readDate);
		return mapper.markAllRead(parameters);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean insertIfAbsent(FmNotification notification) {
		return mapper.insertIfAbsent(notification) == 1;
	}

	@Override
	public List<FmNotification> findPendingEmail(int limit) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("limit", limit);
		return mapper.findPendingEmail(parameters);
	}

	@Override
	public FmNotification findEmailByProviderMessageId(String providerMessageId) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("providerMessageId", providerMessageId);
		return mapper.findEmailByProviderMessageId(parameters);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean markDelivered(String tenantId, String notificationId,
			String providerMessageId, Date sentDate) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("notificationId", notificationId);
		parameters.put("providerMessageId", providerMessageId);
		parameters.put("sentDate", sentDate);
		return mapper.markDelivered(parameters) == 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean markDeliveryAttemptFailed(String tenantId, String notificationId,
			String providerMessageId, Date failedDate, Date nextRetryDate,
			int maxAttempts, String errorMessage) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("notificationId", notificationId);
		parameters.put("providerMessageId", providerMessageId);
		parameters.put("failedDate", failedDate);
		parameters.put("nextRetryDate", nextRetryDate);
		parameters.put("maxAttempts", maxAttempts);
		parameters.put("errorMessage", errorMessage);
		return mapper.markDeliveryAttemptFailed(parameters) == 1;
	}

	private Map<String, Object> identity(String tenantId, String account) {
		Map<String, Object> result = new HashMap<>();
		result.put("tenantId", tenantId);
		result.put("recipientAccount", account);
		return result;
	}
}
