package org.qifu.fm.logic.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.view.FmNotificationInboxView;
import org.qifu.fm.dto.view.FmNotificationView;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.logic.IFmNotificationLogicService;
import org.qifu.fm.service.IFmNotificationService;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FmNotificationLogicServiceImpl implements IFmNotificationLogicService {

	private static final int INBOX_LIMIT = 100;

	private final IFmNotificationService notificationService;
	private final IFmTenantAccountService tenantAccountService;

	public FmNotificationLogicServiceImpl(IFmNotificationService notificationService,
			IFmTenantAccountService tenantAccountService) {
		this.notificationService = notificationService;
		this.tenantAccountService = tenantAccountService;
	}

	@Override
	public DefaultResult<FmNotificationInboxView> inbox(String tenantId) throws ServiceException {
		String account = currentAccount(tenantId);
		var notifications = notificationService.findInbox(tenantId, account, INBOX_LIMIT)
				.stream().map(value -> new FmNotificationView(value.getNotificationId(),
						value.getEventType(), value.getSubject(), value.getContentText(),
						value.getReferenceType(), value.getReferenceId(),
						value.getDeliveryStatus(), value.getCdate(), value.getReadDate()))
				.toList();
		return success(new FmNotificationInboxView(
				notificationService.countUnread(tenantId, account), notifications));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DefaultResult<Long> markRead(String tenantId, String notificationId)
			throws ServiceException {
		if (StringUtils.isBlank(notificationId)) {
			throw new ServiceException("通知編號不可為空");
		}
		String account = currentAccount(tenantId);
		notificationService.markRead(tenantId, account, notificationId.trim(), new Date());
		return success(notificationService.countUnread(tenantId, account));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DefaultResult<Long> markAllRead(String tenantId) throws ServiceException {
		String account = currentAccount(tenantId);
		notificationService.markAllRead(tenantId, account, new Date());
		return success(0L);
	}

	private String currentAccount(String tenantId) throws ServiceException {
		if (StringUtils.isBlank(tenantId)) {
			throw new ServiceException("Tenant 不可為空");
		}
		String account = UserUtils.getCurrentUser().getUsername();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("account", account);
		parameters.put("status", "ACTIVE");
		Date now = new Date();
		boolean active = tenantAccountService.selectListByParams(parameters).getValue().stream()
				.anyMatch(value -> effective(value, now));
		if (!active) {
			throw new ServiceException("目前帳號不屬於指定 Tenant");
		}
		return account;
	}

	private boolean effective(FmTenantAccount value, Date now) {
		return (value.getEffectiveFrom() == null || !value.getEffectiveFrom().after(now))
				&& (value.getEffectiveTo() == null || value.getEffectiveTo().after(now));
	}

	private <T> DefaultResult<T> success(T value) {
		DefaultResult<T> result = new DefaultResult<>();
		result.setSuccess(YesNoKeyProvide.YES);
		result.setValue(value);
		return result;
	}
}
