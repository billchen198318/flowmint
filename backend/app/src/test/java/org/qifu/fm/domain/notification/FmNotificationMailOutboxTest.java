package org.qifu.fm.domain.notification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.entity.TbSysMailHelper;
import org.qifu.core.service.ISysMailHelperService;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.entity.FmNotification;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmNotificationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class FmNotificationMailOutboxTest {

	@Test
	void springContextSelectsProductionConstructor() {
		try (AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext()) {
			context.registerBean(IFmEmployeeService.class,
					() -> mock(IFmEmployeeService.class));
			context.registerBean(IFmNotificationService.class,
					() -> mock(IFmNotificationService.class));
			@SuppressWarnings("rawtypes")
			Class<ISysMailHelperService> mailServiceType = ISysMailHelperService.class;
			context.registerBean(mailServiceType,
					() -> mock(ISysMailHelperService.class));
			context.register(FmNotificationMailOutbox.class);
			context.refresh();
			assertNotNull(context.getBean(FmNotificationMailOutbox.class));
		}
	}

	@Test
	void writesQifuMailHelperForEmployeeWithEmail() throws Exception {
		IFmEmployeeService employees = mock(IFmEmployeeService.class);
		IFmNotificationService notifications = mock(IFmNotificationService.class);
		@SuppressWarnings("unchecked")
		ISysMailHelperService<TbSysMailHelper, String> mails =
				mock(ISysMailHelperService.class);
		FmEmployee employee = new FmEmployee();
		employee.setEmail("approver@example.com");
		DefaultResult<List<FmEmployee>> employeeResult = new DefaultResult<>();
		employeeResult.setValue(List.of(employee));
		when(employees.selectListByParams(anyMap())).thenReturn(employeeResult);
		when(notifications.insertIfAbsent(any())).thenReturn(true);
		when(mails.findForMaxMailIdComplete(any())).thenReturn("202608110001");

		boolean queued = new FmNotificationMailOutbox(
				employees, notifications, mails,
				() -> "flowmint@example.com").enqueue(source());

		assertTrue(queued);
		verify(mails).insert(any(TbSysMailHelper.class));
	}

	@Test
	void keepsInAppNotificationWithoutCreatingMailWhenEmailIsMissing()
			throws Exception {
		IFmEmployeeService employees = mock(IFmEmployeeService.class);
		IFmNotificationService notifications = mock(IFmNotificationService.class);
		@SuppressWarnings("unchecked")
		ISysMailHelperService<TbSysMailHelper, String> mails =
				mock(ISysMailHelperService.class);
		FmEmployee employee = new FmEmployee();
		DefaultResult<List<FmEmployee>> employeeResult = new DefaultResult<>();
		employeeResult.setValue(List.of(employee));
		when(employees.selectListByParams(anyMap())).thenReturn(employeeResult);

		boolean queued = new FmNotificationMailOutbox(
				employees, notifications, mails,
				() -> "flowmint@example.com").enqueue(source());

		assertFalse(queued);
		verify(mails, never()).insert(any(TbSysMailHelper.class));
	}

	private FmNotification source() {
		FmNotification value = new FmNotification();
		value.setNotificationId("N001");
		value.setTenantId("T001");
		value.setRecipientAccount("approver");
		value.setEventType("TASK_ASSIGNED");
		value.setSubject("新待辦");
		value.setContentText("請處理待辦");
		value.setReferenceType("TASK");
		value.setReferenceId("TASK-1");
		value.setCuserid("starter");
		value.setCdate(new Date());
		return value;
	}
}
