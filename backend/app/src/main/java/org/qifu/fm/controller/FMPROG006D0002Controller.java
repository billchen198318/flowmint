package org.qifu.fm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.qifu.base.exception.ControllerException;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.CheckControllerFieldHandler;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmDataActionCommand;
import org.qifu.fm.dto.command.FmDataActionPreviewCommand;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.dto.view.FmDataActionView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmDataActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG006D0002")
public class FMPROG006D0002Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmDataActionService actionService;
	private final transient IFmDataActionLogicService actionLogicService;

	public FMPROG006D0002Controller(
			IFmDataActionService actionService,
			IFmDataActionLogicService actionLogicService) {
		this.actionService = actionService;
		this.actionLogicService = actionLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmDataActionView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmDataActionView>> result = initResult();
		try {
			QueryResult<List<FmDataAction>> query = actionService.findPage(
					queryParameter(body)
							.fullEquals("tenantId")
							.fullEquals("status")
							.fullLink("actionCodeLike")
							.value(),
					body.getPageOf().orderBy("ACTION_CODE").sortTypeAsc());
			QueryResult<List<FmDataActionView>> view = new QueryResult<>();
			view.setValue(Objects.requireNonNullElse(
					query.getValue(), List.<FmDataAction>of()).stream()
					.map(action -> {
						try {
							return actionLogicService.view(action);
						} catch (ServiceException exception) {
							throw new DataActionViewRuntimeException(exception);
						}
					}).toList());
			view.setMessage(query.getMessage());
			setQueryResponseJsonResult(view, result, body.getPageOf());
		} catch (Exception exception) {
			Throwable cause = exception instanceof DataActionViewRuntimeException
					? exception.getCause() : exception;
			noSuccessResult(result, cause instanceof Exception causeException
					? causeException : exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionView>> save(
			@RequestBody FmDataActionCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionView>> update(
			@RequestBody FmDataActionCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmDataActionView>> command(
			FmDataActionCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmDataActionView> result =
				initDefaultJsonResult();
		try {
			validateCommand(result, command, create);
			DefaultResult<FmDataActionView> data = create
					? actionLogicService.create(command)
					: actionLogicService.update(command);
			setDefaultResponseJsonResult(data, result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@SuppressWarnings("rawtypes")
	private void validateCommand(
			DefaultControllerJsonResultObj<FmDataActionView> result,
			FmDataActionCommand command, boolean create)
			throws ControllerException, ServiceException {
		CheckControllerFieldHandler check =
				getCheckControllerFieldHandler(result);
		if (!create) {
			check.testField("oid", command,
					"@org.apache.commons.lang3.StringUtils@isBlank(oid)",
					"缺少 Data Action OID");
		}
		check.testField("tenantId", command,
				"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
				"請選擇 Tenant")
				.testField("actionCode", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(actionCode)",
						"請輸入 Action Code")
				.testField("actionName", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(actionName)",
						"請輸入 Action 名稱")
				.testField("poolId", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(poolId)",
						"請選擇 DataSource Pool")
				.testField("actionType", command,
						"@org.apache.commons.lang3.StringUtils@isBlank(actionType)",
						"請選擇 Action Type")
				.throwMessage();
		if (command.steps() == null || command.steps().isEmpty()) {
			throw new ServiceException("至少需要一個 SQL Step");
		}
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmDataActionView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					actionLogicService.load(body.get("oid"), null), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002U", check = true)
	@PostMapping("/publish")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionView>> publish(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmDataActionView> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					actionLogicService.publish(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002E", check = true)
	@PostMapping("/{actionId}/versions/{versionNo}/preview")
	public ResponseEntity<DefaultControllerJsonResultObj<FmDataActionExecutionView>> preview(
			@PathVariable String actionId,
			@PathVariable Integer versionNo,
			@RequestBody FmDataActionPreviewCommand command) {
		DefaultControllerJsonResultObj<FmDataActionExecutionView> result =
				initDefaultJsonResult();
		try {
			String account = UserUtils.getCurrentUser().getUsername();
			setDefaultResponseJsonResult(actionLogicService.preview(
					command.tenantId(), actionId, versionNo,
					safeRequest(command.request()), account),
					result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG006D0002Q", check = true)
	@PostMapping("/pool-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> poolOptions(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result =
				initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(actionLogicService.poolOptions(
					body.get("tenantId")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private Map<String, Object> safeRequest(Map<String, Object> request) {
		return request == null ? new HashMap<>() : request;
	}

	private static final class DataActionViewRuntimeException
			extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private DataActionViewRuntimeException(ServiceException cause) {
			super(cause);
		}
	}
}
