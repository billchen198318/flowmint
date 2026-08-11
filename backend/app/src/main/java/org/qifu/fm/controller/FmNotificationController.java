package org.qifu.fm.controller;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmNotificationReadRequest;
import org.qifu.fm.dto.view.FmNotificationInboxView;
import org.qifu.fm.logic.IFmNotificationLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/fm/notifications")
public class FmNotificationController extends CoreApiSupport {

	private final IFmNotificationLogicService notificationLogicService;

	public FmNotificationController(IFmNotificationLogicService notificationLogicService) {
		this.notificationLogicService = notificationLogicService;
	}

	@PostMapping("/inbox")
	public ResponseEntity<DefaultControllerJsonResultObj<FmNotificationInboxView>> inbox(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<FmNotificationInboxView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(notificationLogicService.inbox(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/read")
	public ResponseEntity<DefaultControllerJsonResultObj<Long>> read(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmNotificationReadRequest request) {
		DefaultControllerJsonResultObj<Long> result = initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("通知參數不可為空");
			}
			setDefaultResponseJsonResult(notificationLogicService.markRead(
					tenantId, request.notificationId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/read-all")
	public ResponseEntity<DefaultControllerJsonResultObj<Long>> readAll(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<Long> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(notificationLogicService.markAllRead(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}
}
