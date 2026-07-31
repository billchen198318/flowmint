package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ControllerException;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.CheckControllerFieldHandler;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmEmployeeCommand;
import org.qifu.fm.dto.view.FmEmployeeView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmEmployee;
import org.qifu.fm.logic.IFmEmployeeLogicService;
import org.qifu.fm.service.IFmEmployeeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG002D0001")
public class FMPROG002D0001Controller extends CoreApiSupport {
	private static final long serialVersionUID = 1L;
	private final transient IFmEmployeeService employeeService;
	private final transient IFmEmployeeLogicService employeeLogicService;

	public FMPROG002D0001Controller(IFmEmployeeService employeeService,
			IFmEmployeeLogicService employeeLogicService) {
		this.employeeService = employeeService;
		this.employeeLogicService = employeeLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001Q", check = true)
	@PostMapping(value = "/findPage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<QueryResult<List<FmEmployeeView>>> findPage(@RequestBody SearchBody searchBody) {
		QueryResult<List<FmEmployeeView>> result = this.initResult();
		try {
			QueryResult<List<FmEmployee>> query = employeeService.findPage(
					this.queryParameter(searchBody).fullEquals("tenantId").fullEquals("status")
							.fullLink("employeeNoLike").fullLink("accountLike").fullLink("displayNameLike").value(),
					searchBody.getPageOf().orderBy("TENANT_ID,EMPLOYEE_NO").sortTypeAsc());
			QueryResult<List<FmEmployeeView>> viewResult = new QueryResult<>();
			viewResult.setValue(query.getValue() == null ? List.of()
					: query.getValue().stream().map(FmEmployeeView::from).toList());
			viewResult.setMessage(query.getMessage());
			this.setQueryResponseJsonResult(viewResult, result, searchBody.getPageOf());
		} catch (ServiceException | ControllerException e) {
			this.noSuccessResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001C", check = true)
	@PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmEmployeeView>> save(@RequestBody FmEmployeeCommand command) {
		DefaultControllerJsonResultObj<FmEmployeeView> result = this.initDefaultJsonResult();
		try {
			validate(result, command);
			this.setDefaultResponseJsonResult(employeeLogicService.create(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001E", check = true)
	@PostMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmEmployeeView>> load(@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmEmployeeView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(
					employeeLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001U", check = true)
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmEmployeeView>> update(
			@RequestBody FmEmployeeCommand command) {
		DefaultControllerJsonResultObj<FmEmployeeView> result = this.initDefaultJsonResult();
		try {
			validate(result, command);
			this.setDefaultResponseJsonResult(employeeLogicService.update(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001D", check = true)
	@PostMapping(value = "/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmEmployeeView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmEmployeeView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(employeeLogicService.deactivate(body.get("oid")), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001Q", check = true)
	@PostMapping(value = "/tenant-options", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(employeeLogicService.tenantOptions(), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0001Q", check = true)
	@PostMapping(value = "/account-options", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> accountOptions(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(employeeLogicService.accountOptions(body.get("tenantId")), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	private void validate(DefaultControllerJsonResultObj<FmEmployeeView> result, FmEmployeeCommand command)
			throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = this.getCheckControllerFieldHandler(result);
		check.testField("tenantId", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
				"請選擇 Tenant")
				.testField("employeeNo", command, "@org.apache.commons.lang3.StringUtils@isBlank(employeeNo)",
						"請輸入員工編號")
				.testField("account", command, "@org.apache.commons.lang3.StringUtils@isBlank(account)", "請選擇帳號")
				.testField("displayName", command, "@org.apache.commons.lang3.StringUtils@isBlank(displayName)",
						"請輸入顯示名稱")
				.testField("effectiveFrom", command, "effectiveFrom == null", "請輸入生效時間")
				.throwHtmlMessage();
		if (command.effectiveFrom() != null && command.effectiveTo() != null
				&& !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException("失效時間必須晚於生效時間");
		}
	}
}
