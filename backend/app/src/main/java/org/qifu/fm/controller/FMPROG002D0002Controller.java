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
import org.qifu.fm.dto.command.FmOrgUnitCommand;
import org.qifu.fm.dto.command.FmOrgUnitDeactivateCommand;
import org.qifu.fm.dto.command.FmOrgUnitMoveCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitMovePreviewView;
import org.qifu.fm.dto.view.FmOrgUnitView;
import org.qifu.fm.logic.IFmOrgUnitLogicService;
import org.qifu.fm.service.IFmOrgUnitVersionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG002D0002")
public class FMPROG002D0002Controller extends CoreApiSupport {
	private static final long serialVersionUID = 1L;

	private final transient IFmOrgUnitVersionService orgUnitVersionService;
	private final transient IFmOrgUnitLogicService orgUnitLogicService;

	public FMPROG002D0002Controller(IFmOrgUnitVersionService orgUnitVersionService,
			IFmOrgUnitLogicService orgUnitLogicService) {
		this.orgUnitVersionService = orgUnitVersionService;
		this.orgUnitLogicService = orgUnitLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002Q", check = true)
	@PostMapping(value = "/findPage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<QueryResult<List<FmOrgUnitView>>> findPage(@RequestBody SearchBody searchBody) {
		QueryResult<List<FmOrgUnitView>> result = this.initResult();
		try {
			QueryResult<List<FmOrgUnitView>> query = orgUnitVersionService.findCurrentPage(
					this.queryParameter(searchBody)
							.fullEquals("tenantId")
							.fullEquals("status")
							.fullLink("unitCodeLike")
							.fullLink("unitNameLike")
							.value(),
					searchBody.getPageOf().orderBy("PATH,SORT_NO,UNIT_CODE").sortTypeAsc());
			this.setQueryResponseJsonResult(query, result, searchBody.getPageOf());
		} catch (Exception exception) {
			this.noSuccessResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002C", check = true)
	@PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitView>> save(
			@RequestBody FmOrgUnitCommand command) {
		DefaultControllerJsonResultObj<FmOrgUnitView> result = this.initDefaultJsonResult();
		try {
			validate(result, command);
			this.setDefaultResponseJsonResult(orgUnitLogicService.create(command), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002E", check = true)
	@PostMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgUnitView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(
					orgUnitLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002U", check = true)
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitView>> update(
			@RequestBody FmOrgUnitCommand command) {
		DefaultControllerJsonResultObj<FmOrgUnitView> result = this.initDefaultJsonResult();
		try {
			validate(result, command);
			this.setDefaultResponseJsonResult(orgUnitLogicService.update(command), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002D", check = true)
	@PostMapping(value = "/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitView>> deactivate(
			@RequestBody FmOrgUnitDeactivateCommand command) {
		DefaultControllerJsonResultObj<FmOrgUnitView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(
					orgUnitLogicService.deactivate(command.oid(), command.currentVersionNo()), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002Q", check = true)
	@PostMapping(value = "/tree", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOrgUnitView>>> tree(
			@RequestBody Map<String, Object> body) {
		DefaultControllerJsonResultObj<List<FmOrgUnitView>> result = this.initDefaultJsonResult();
		try {
			boolean includeInactive = Boolean.TRUE.equals(body.get("includeInactive"));
			this.setDefaultResponseJsonResult(
					orgUnitLogicService.tree(String.valueOf(body.get("tenantId")), includeInactive), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002U", check = true)
	@PostMapping(value = "/move-preview", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitMovePreviewView>> previewMove(
			@RequestBody FmOrgUnitMoveCommand command) {
		DefaultControllerJsonResultObj<FmOrgUnitMovePreviewView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(orgUnitLogicService.previewMove(command), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002U", check = true)
	@PostMapping(value = "/move", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOrgUnitView>>> move(
			@RequestBody FmOrgUnitMoveCommand command) {
		DefaultControllerJsonResultObj<List<FmOrgUnitView>> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(orgUnitLogicService.move(command), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0002Q", check = true)
	@PostMapping(value = "/tenant-options", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(orgUnitLogicService.tenantOptions(), result);
		} catch (ServiceException | ControllerException exception) {
			this.exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	private void validate(DefaultControllerJsonResultObj<FmOrgUnitView> result, FmOrgUnitCommand command)
			throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = this.getCheckControllerFieldHandler(result);
		check.testField("tenantId", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
				"請選擇 Tenant")
				.testField("unitCode", command, "@org.apache.commons.lang3.StringUtils@isBlank(unitCode)",
						"請輸入部門代碼")
				.testField("unitName", command, "@org.apache.commons.lang3.StringUtils@isBlank(unitName)",
						"請輸入部門名稱")
				.testField("effectiveFrom", command, "effectiveFrom == null", "請輸入生效時間")
				.throwHtmlMessage();
		if (command.effectiveFrom() != null && command.effectiveTo() != null
				&& !command.effectiveTo().after(command.effectiveFrom())) {
			throw new ServiceException("失效時間必須晚於生效時間");
		}
	}
}
