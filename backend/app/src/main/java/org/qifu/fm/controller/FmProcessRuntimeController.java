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
import org.qifu.fm.dto.command.FmTaskAddSignRequest;
import org.qifu.fm.dto.command.FmTaskLoadRequest;
import org.qifu.fm.dto.command.FmTaskTransferRequest;
import org.qifu.fm.dto.command.FmTaskDelegationRequest;
import org.qifu.fm.dto.command.FmTaskResolveRequest;
import org.qifu.fm.dto.command.FmParallelAddSignCancelRequest;
import org.qifu.fm.dto.command.FmParallelAddSignCompleteRequest;
import org.qifu.fm.dto.command.FmParallelAddSignStartRequest;
import org.qifu.fm.dto.view.FmProcessStartCatalogView;
import org.qifu.fm.dto.view.FmProcessStartApplicantView;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.dto.view.FmRuntimeTenantView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.qifu.fm.dto.view.FmTaskInboxView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmParallelAddSignDetailView;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
import org.qifu.fm.logic.IFmParallelAddSignRuntimeLogicService;
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
	private final transient IFmParallelAddSignRuntimeLogicService parallelAddSignLogicService;

	public FmProcessRuntimeController(
			IFmProcessRuntimeLogicService runtimeLogicService,
			IFmTaskRuntimeLogicService taskRuntimeLogicService,
			IFmRequestTrackingLogicService trackingLogicService,
			IFmParallelAddSignRuntimeLogicService parallelAddSignLogicService) {
		this.runtimeLogicService = runtimeLogicService;
		this.taskRuntimeLogicService = taskRuntimeLogicService;
		this.trackingLogicService = trackingLogicService;
		this.parallelAddSignLogicService = parallelAddSignLogicService;
	}

	@PostMapping("/tasks/parallel-add-sign-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>>
			parallelAddSignOptions(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmTaskLoadRequest request) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("Task 必填");
			}
			setDefaultResponseJsonResult(parallelAddSignLogicService.options(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/start-parallel-add-sign")
	public ResponseEntity<DefaultControllerJsonResultObj<FmParallelAddSignDetailView>>
			startParallelAddSign(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmParallelAddSignStartRequest request) {
		DefaultControllerJsonResultObj<FmParallelAddSignDetailView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					parallelAddSignLogicService.start(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/complete-parallel-add-sign")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>>
			completeParallelAddSign(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmParallelAddSignCompleteRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					parallelAddSignLogicService.complete(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/cancel-parallel-add-sign")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>>
			cancelParallelAddSign(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmParallelAddSignCancelRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					parallelAddSignLogicService.cancel(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/parallel-add-sign-detail")
	public ResponseEntity<DefaultControllerJsonResultObj<FmParallelAddSignDetailView>>
			parallelAddSignDetail(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmTaskLoadRequest request) {
		DefaultControllerJsonResultObj<FmParallelAddSignDetailView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("Task 必填");
			}
			setDefaultResponseJsonResult(parallelAddSignLogicService.detail(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
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

	@PostMapping("/tasks/delegate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> delegate(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskDelegationRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.delegate(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/resolve")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> resolve(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskResolveRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.resolve(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/add-sign-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> addSignOptions(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskLoadRequest request) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("Task 不可為空");
			}
			setDefaultResponseJsonResult(taskRuntimeLogicService.addSignOptions(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/add-sign")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> addSign(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskAddSignRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.addSign(tenantId, request), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/tasks/complete-add-sign")
	public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> completeAddSign(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmTaskResolveRequest request) {
		DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(taskRuntimeLogicService.completeAddSign(
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

	@PostMapping("/start/applicants")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmProcessStartApplicantView>>> applicants(
			@RequestHeader("X-FlowMint-Tenant") String tenantId) {
		DefaultControllerJsonResultObj<List<FmProcessStartApplicantView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					runtimeLogicService.applicants(tenantId), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/mine/diagram")
	public ResponseEntity<DefaultControllerJsonResultObj<FmRequestProcessDiagramView>> diagramMine(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmRequestTrackLoadRequest request) {
		DefaultControllerJsonResultObj<FmRequestProcessDiagramView> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("流程實例資料不可為空");
			}
			setDefaultResponseJsonResult(trackingLogicService.diagram(
					tenantId, request.processInstanceId()), result);
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
							request.formData(),
							request.uploadSessionId())), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

}
