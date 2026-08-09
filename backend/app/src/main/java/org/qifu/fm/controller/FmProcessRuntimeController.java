package org.qifu.fm.controller;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmProcessStartLoadCommand;
import org.qifu.fm.dto.command.FmProcessStartLoadRequest;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.command.FmProcessSubmitRequest;
import org.qifu.fm.dto.view.FmProcessStartLoadView;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.logic.IFmProcessRuntimeLogicService;
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

	public FmProcessRuntimeController(
			IFmProcessRuntimeLogicService runtimeLogicService) {
		this.runtimeLogicService = runtimeLogicService;
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
