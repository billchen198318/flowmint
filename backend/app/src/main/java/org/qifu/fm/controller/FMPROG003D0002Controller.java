package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmWorkflowDelegationCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmWorkflowDelegationView;
import org.qifu.fm.entity.FmWorkflowDelegation;
import org.qifu.fm.logic.IFmWorkflowDelegationLogicService;
import org.qifu.fm.service.IFmWorkflowDelegationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG003D0002")
public class FMPROG003D0002Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmWorkflowDelegationService delegationService;
	private final transient IFmWorkflowDelegationLogicService delegationLogicService;

	public FMPROG003D0002Controller(
			IFmWorkflowDelegationService delegationService,
			IFmWorkflowDelegationLogicService delegationLogicService) {
		this.delegationService = delegationService;
		this.delegationLogicService = delegationLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmWorkflowDelegationView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmWorkflowDelegationView>> result = initResult();
		try {
			QueryResult<List<FmWorkflowDelegation>> query = delegationService.findPage(
					queryParameter(body).fullEquals("tenantId").fullEquals("principalAccount")
							.fullEquals("delegateAccount").fullEquals("scopeType")
							.fullEquals("status").value(),
					body.getPageOf().orderBy("TENANT_ID,PRINCIPAL_ACCOUNT,EFFECTIVE_FROM")
							.sortTypeAsc());
			QueryResult<List<FmWorkflowDelegationView>> view = new QueryResult<>();
			view.setValue(query.getValue() == null ? List.of() : query.getValue().stream()
					.map(value -> {
						try {
							return delegationLogicService.view(value);
						} catch (Exception exception) {
							throw new IllegalStateException(exception);
						}
					}).toList());
			view.setMessage(query.getMessage());
			setQueryResponseJsonResult(view, result, body.getPageOf());
		} catch (Exception exception) {
			noSuccessResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmWorkflowDelegationView>> save(
			@RequestBody FmWorkflowDelegationCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmWorkflowDelegationView>> update(
			@RequestBody FmWorkflowDelegationCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmWorkflowDelegationView>> command(
			FmWorkflowDelegationCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmWorkflowDelegationView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("tenantId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
					.testField("principalAccount", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(principalAccount)", "請選擇被代理人")
					.testField("delegateAccount", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(delegateAccount)", "請選擇代理人")
					.testField("scopeType", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(scopeType)", "請選擇代理範圍")
					.testField("effectiveFrom", command, "effectiveFrom==null", "請輸入開始時間")
					.testField("effectiveTo", command, "effectiveTo==null", "請輸入結束時間")
					.testField("reason", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(reason)", "請輸入代理原因")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(create ? delegationLogicService.create(command)
					: delegationLogicService.update(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmWorkflowDelegationView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmWorkflowDelegationView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(delegationLogicService.load(
					body.get("oid"), BaseSystemMessage.dataIsExist()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmWorkflowDelegationView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmWorkflowDelegationView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(delegationLogicService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		return options(delegationLogicService.tenantOptions());
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002Q", check = true)
	@PostMapping("/account-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> accountOptions(
			@RequestBody Map<String, String> body) {
		return options(delegationLogicService.accountOptions(body.get("tenantId")));
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0002Q", check = true)
	@PostMapping("/group-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> groupOptions(
			@RequestBody Map<String, String> body) {
		return options(delegationLogicService.groupOptions(body.get("tenantId")));
	}

	private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(
			DefaultResult<List<FmOptionView>> data) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(data, result);
		return ResponseEntity.ok(result);
	}
}
