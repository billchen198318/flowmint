package org.qifu.fm.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmProcessStartCatalogCommand;
import org.qifu.fm.dto.command.FmProcessStartCatalogRequest;
import org.qifu.fm.dto.command.FmProcessStartLoadCommand;
import org.qifu.fm.dto.command.FmProcessStartLoadRequest;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.command.FmProcessSubmitRequest;
import org.qifu.fm.dto.command.FmRequestTrackLoadRequest;
import org.qifu.fm.dto.command.FmRequestWithdrawRequest;
import org.qifu.fm.dto.command.FmRequestCancelRequest;
import org.qifu.fm.dto.command.FmTaskActionRequest;
import org.qifu.fm.dto.command.FmTaskLoadRequest;
import org.qifu.fm.dto.command.FmTaskTransferRequest;
import org.qifu.fm.dto.view.FmProcessStartCatalogView;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.dto.view.FmRuntimeTenantView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmRequestTrackView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.dto.view.FmTaskInboxView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.logic.IFmRequestTrackingLogicService;
import org.qifu.fm.logic.IFmTaskRuntimeLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/fm/requests")
public class FmProcessRuntimeController extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmProcessRuntimeLogicService runtimeLogicService;
	private final transient IFmTaskRuntimeLogicService taskRuntimeLogicService;
	private final transient IFmRequestTrackingLogicService trackingLogicService;

	public FmProcessRuntimeController(
			IFmProcessRuntimeLogicService runtimeLogicService,
			IFmTaskRuntimeLogicService taskRuntimeLogicService,
			IFmRequestTrackingLogicService trackingLogicService) {
		this.runtimeLogicService = runtimeLogicService;
		this.taskRuntimeLogicService = taskRuntimeLogicService;
		this.trackingLogicService = trackingLogicService;
	}

	@PostMapping("/mine")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmRequestTrackView>>> mine(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<List<FmRequestTrackView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(trackingLogicService.mine(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/mine/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmRequestTrackDetailView>> loadMine(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmRequestTrackLoadRequest request) {
		DefaultControllerJsonResultObj<FmRequestTrackDetailView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("申請進度參數不可為空");
			}
			setDefaultResponseJsonResult(trackingLogicService.load(
					tenantId, request.processInstanceId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/mine/withdraw")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> withdraw(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmRequestWithdrawRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("撤回參數不可為空");
			}
			setDefaultResponseJsonResult(trackingLogicService.withdraw(
					tenantId, request.processInstanceId(), request.reason()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/mine/cancel")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> cancel(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmRequestCancelRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("取消參數不可為空");
			}
			setDefaultResponseJsonResult(trackingLogicService.cancel(
					tenantId, request.processInstanceId(), request.reason()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/inbox")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmTaskInboxView>>> inbox(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<List<FmTaskInboxView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.inbox(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskDetailView>> loadTask(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskLoadRequest request) {
		DefaultControllerJsonResultObj<FmTaskDetailView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("待辦載入參數不可為空");
			}
			setDefaultResponseJsonResult(taskRuntimeLogicService.load(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/action")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> action(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskActionRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.action(
					tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/transfer-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> transferOptions(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskLoadRequest request) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("轉派選項參數不可為空");
			}
			setDefaultResponseJsonResult(taskRuntimeLogicService.transferOptions(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/transfer")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> transfer(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskTransferRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("轉派參數不可為空");
			}
			setDefaultResponseJsonResult(taskRuntimeLogicService.transfer(
					tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/start/tenants")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmRuntimeTenantView>>> tenants() {
		DefaultControllerJsonResultObj<List<FmRuntimeTenantView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(runtimeLogicService.tenants(), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/start/catalog")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmProcessStartCatalogView>>> catalog(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmProcessStartCatalogRequest request) {
		DefaultControllerJsonResultObj<List<FmProcessStartCatalogView>> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("起單清單參數不可為空");
			}
			setDefaultResponseJsonResult(runtimeLogicService.catalog(
					new FmProcessStartCatalogCommand(
							tenantId, request.applicantAccount())), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/start/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmProcessStartLoadView>> loadStart(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmProcessStartLoadRequest request) {
		DefaultControllerJsonResultObj<FmProcessStartLoadView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("起單載入參數不可為空");
			}
			setDefaultResponseJsonResult(runtimeLogicService.loadStart(
					new FmProcessStartLoadCommand(
							tenantId,
							request.processDefId(),
							request.applicantAccount())), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/submit")
	public ResponseEntity<DefaultControllerJsonResultObj<FmProcessSubmitView>> submit(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestBody FmProcessSubmitRequest request) {
		DefaultControllerJsonResultObj<FmProcessSubmitView> result =
				initDefaultJsonResult();
		try {
			if (request == null || StringUtils.isBlank(idempotencyKey)) {
				throw new ServiceException("送單參數或 Idempotency-Key 不可為空");
			}
			setDefaultResponseJsonResult(runtimeLogicService.submit(
					new FmProcessSubmitCommand(
							tenantId,
							request.processDefId(),
							request.formId(),
							request.formVersionNo(),
							idempotencyKey,
							request.applicantAccount(),
							request.formData())), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

}
