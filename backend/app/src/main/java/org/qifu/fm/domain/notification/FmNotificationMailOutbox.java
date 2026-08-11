package org.qifu.fm.domain.notification;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.YesNoKeyProvide;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.core.util.SystemSettingConfigureUtils;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmNotificationService;
import org.qifu.util.SimpleUtils;
import org.springframework.stereotype.Component;

@Component
public class FmNotificationMailOutbox {

	private final IFmEmployeeService employeeService;
	private final IFmNotificationService notificationService;
	private final ISysMailHelperService<TbSysMailHelper, String> mailHelperService;
	private final Supplier<String> defaultMailFrom;

	public FmNotificationMailOutbox(
			IFmEmployeeService employeeService,
			IFmNotificationService notificationService,
			ISysMailHelperService<TbSysMailHelper, String> mailHelperService) {
		this.employeeService = employeeService;
		this.notificationService = notificationService;
		this.mailHelperService = mailHelperService;
		this.defaultMailFrom = SystemSettingConfigureUtils::getMailDefaultFromValue;
	}

	FmNotificationMailOutbox(
			IFmEmployeeService employeeService,
			IFmNotificationService notificationService,
			ISysMailHelperService<TbSysMailHelper, String> mailHelperService,
			Supplier<String> defaultMailFrom) {
		this.employeeService = employeeService;
		this.notificationService = notificationService;
		this.mailHelperService = mailHelperService;
		this.defaultMailFrom = defaultMailFrom;
	}

	public boolean enqueue(FmNotification source) throws ServiceException {
		FmEmployee employee = activeEmployee(
				source.getTenantId(), source.getRecipientAccount(), new Date());
		if (employee == null || StringUtils.isBlank(employee.getEmail())) {
			return false;
		}
		FmNotification emailNotification = emailNotification(source);
		String mailDate = SimpleUtils.getStrYMD("");
		String mailId = mailHelperService.findForMaxMailIdComplete(mailDate);
		emailNotification.setProviderMessageId(mailId);
		if (!notificationService.insertIfAbsent(emailNotification)) {
			return false;
		}
		TbSysMailHelper mail = new TbSysMailHelper();
		mail.setMailId(mailId);
		mail.setMailFrom(defaultMailFrom.get());
		mail.setMailTo(employee.getEmail().trim());
		mail.setSubject(source.getSubject());
		mail.setText(source.getContentText().getBytes(StandardCharsets.UTF_8));
		mail.setRetainFlag(YesNoKeyProvide.YES);
		mail.setSuccessFlag(YesNoKeyProvide.NO);
		mail.setCuserid(source.getCuserid());
		mail.setCdate(source.getCdate());
		mailHelperService.insert(mail);
		return true;
	}

	private FmEmployee activeEmployee(String tenantId, String account, Date now)
			throws ServiceException {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tenantId", tenantId);
		parameters.put("account", account);
		parameters.put("status", "ACTIVE");
		return employeeService.selectListByParams(parameters).getValue().stream()
				.filter(value -> effective(value, now)).findFirst().orElse(null);
	}

	private boolean effective(FmEmployee employee, Date now) {
		return (employee.getEffectiveFrom() == null
				|| !employee.getEffectiveFrom().after(now))
				&& (employee.getEffectiveTo() == null
				|| employee.getEffectiveTo().after(now));
	}

	private FmNotification emailNotification(FmNotification source) {
		FmNotification notification = new FmNotification();
		notification.setOid(UUID.randomUUID().toString());
		notification.setTenantId(source.getTenantId());
		notification.setNotificationId(UUID.nameUUIDFromBytes((source.getNotificationId()
				+ ":EMAIL").getBytes(StandardCharsets.UTF_8)).toString());
		notification.setRecipientAccount(source.getRecipientAccount());
		notification.setChannelType("EMAIL");
		notification.setEventType(source.getEventType());
		notification.setSubject(source.getSubject());
		notification.setContentText(source.getContentText());
		notification.setReferenceType(source.getReferenceType());
		notification.setReferenceId(source.getReferenceId());
		notification.setDeliveryStatus("PENDING");
		notification.setRetryCount(0);
		notification.setCuserid(source.getCuserid());
		notification.setCdate(source.getCdate());
		return notification;
	}
}
