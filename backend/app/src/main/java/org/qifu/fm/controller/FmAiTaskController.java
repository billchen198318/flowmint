package org.qifu.fm.controller;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmAiProviderOptionsRequest;
import org.qifu.fm.dto.command.FmAiAnalysisCommand;
import org.qifu.fm.dto.view.FmAiAnalysisView;
import org.qifu.fm.dto.view.FmAiProviderOptionView;
import org.qifu.fm.logic.IFmAiTaskRuntimeLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/fm/tasks")
public class FmAiTaskController extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmAiTaskRuntimeLogicService aiTaskRuntimeLogicService;

	public FmAiTaskController(IFmAiTaskRuntimeLogicService aiTaskRuntimeLogicService) {
		this.aiTaskRuntimeLogicService = aiTaskRuntimeLogicService;
	}

	@PostMapping("/ai-provider-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmAiProviderOptionView>>>
			providerOptions(
					@RequestHeader("X-FlowMint-Tenant") String tenantId,
					@RequestBody FmAiProviderOptionsRequest request) {
		DefaultControllerJsonResultObj<List<FmAiProviderOptionView>> result =
				initDefaultJsonResult();
		try {
			if (request == null) {
				throw new ServiceException("Task 必填");
			}
			setDefaultResponseJsonResult(aiTaskRuntimeLogicService.providerOptions(
					tenantId, request.taskId()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/ai-analysis")
	public ResponseEntity<DefaultControllerJsonResultObj<FmAiAnalysisView>> analyze(
			@RequestHeader("X-FlowMint-Tenant") String tenantId,
			@RequestBody FmAiAnalysisCommand command) {
		DefaultControllerJsonResultObj<FmAiAnalysisView> result = initDefaultJsonResult();
		try {
			if (command == null) {
				throw new ServiceException("缺少 AI 分析參數");
			}
			setDefaultResponseJsonResult(
					aiTaskRuntimeLogicService.analyze(tenantId, command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}
}
