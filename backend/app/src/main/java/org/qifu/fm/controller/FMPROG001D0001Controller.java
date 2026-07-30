package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.exception.ControllerException;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.CheckControllerFieldHandler;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmTenantAccountCommand;
import org.qifu.fm.dto.command.FmTenantCommand;
import org.qifu.fm.dto.view.FmTenantView;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.logic.IFmTenantLogicService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG001D0001")
public class FMPROG001D0001Controller extends CoreApiSupport {
	private static final long serialVersionUID = 1L;
	private final transient IFmTenantService tenantService;
	private final transient IFmTenantLogicService tenantLogicService;

	public FMPROG001D0001Controller(IFmTenantService tenantService, IFmTenantLogicService tenantLogicService) {
		this.tenantService = tenantService;
		this.tenantLogicService = tenantLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001Q", check = true)
	@PostMapping(value = "/findPage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<QueryResult<List<FmTenantView>>> findPage(@RequestBody SearchBody searchBody) {
		QueryResult<List<FmTenantView>> result = this.initResult();
		try {
			QueryResult<List<FmTenant>> query = tenantService.findPage(
					this.queryParameter(searchBody).fullEquals("status").fullLink("tenantCodeLike")
							.fullLink("tenantNameLike").value(),
					searchBody.getPageOf().orderBy("TENANT_CODE").sortTypeAsc());
			result.setSuccess(query.getSuccess());
			result.setMessage(query.getMessage());
			result.setIsAuth(query.getIsAuth());
			result.setPageOf(query.getPageOf());
			result.setValue(query.getValue().stream().map(v -> FmTenantView.from(v, List.of())).toList());
		} catch (ServiceException | ControllerException e) {
			this.noSuccessResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001C", check = true)
	@PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> save(@RequestBody FmTenantCommand command) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			validateTenant(result, command, true);
			this.setDefaultResponseJsonResult(tenantLogicService.create(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001E", check = true)
	@PostMapping(value = "/load", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> load(@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(tenantLogicService.load(body.get("oid")), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001U", check = true)
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> update(@RequestBody FmTenantCommand command) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			validateTenant(result, command, false);
			this.setDefaultResponseJsonResult(tenantLogicService.update(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001D", check = true)
	@PostMapping(value = "/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			this.setDefaultResponseJsonResult(tenantLogicService.deactivate(body.get("oid")), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001U", check = true)
	@PostMapping(value = "/account/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> addAccount(
			@RequestBody FmTenantAccountCommand command) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			validateAccount(result, command);
			this.setDefaultResponseJsonResult(tenantLogicService.addAccount(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG001D0001U", check = true)
	@PostMapping(value = "/account/update", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DefaultControllerJsonResultObj<FmTenantView>> updateAccount(
			@RequestBody FmTenantAccountCommand command) {
		DefaultControllerJsonResultObj<FmTenantView> result = this.initDefaultJsonResult();
		try {
			validateAccount(result, command);
			this.setDefaultResponseJsonResult(tenantLogicService.updateAccount(command), result);
		} catch (ServiceException | ControllerException e) {
			this.exceptionResult(result, e);
		}
		return ResponseEntity.ok(result);
	}

	private void validateTenant(DefaultControllerJsonResultObj<FmTenantView> result, FmTenantCommand command,
			boolean create) throws ControllerException, ServiceException {
		@SuppressWarnings("rawtypes")
		CheckControllerFieldHandler check = this.getCheckControllerFieldHandler(result);
		if (create)
			check.testField("tenantId", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
					"請輸入 Tenant ID");
		check.testField("tenantCode", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantCode)",
				"請輸入 Tenant 代碼")
				.testField("tenantName", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantName)",
						"請輸入 Tenant 名稱")
				.throwHtmlMessage();
	}

	private void validateAccount(DefaultControllerJsonResultObj<FmTenantView> result, FmTenantAccountCommand command)
			throws ControllerException, ServiceException {
		this.getCheckControllerFieldHandler(result)
				.testField("tenantOid", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantOid)",
						"缺少 Tenant")
				.testField("account", command, "@org.apache.commons.lang3.StringUtils@isBlank(account)", "請輸入帳號")
				.testField("effectiveFrom", command, "effectiveFrom == null", "請輸入生效時間").throwHtmlMessage();
	}
}