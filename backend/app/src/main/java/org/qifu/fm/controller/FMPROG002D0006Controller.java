package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmEmployeeDutyCommand;
import org.qifu.fm.dto.command.FmOrgDutyCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgDutyView;
import org.qifu.fm.entity.FmOrgDuty;
import org.qifu.fm.logic.IFmOrgDutyLogicService;
import org.qifu.fm.service.IFmOrgDutyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG002D0006")
public class FMPROG002D0006Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmOrgDutyService dutyService;
	private final transient IFmOrgDutyLogicService dutyLogicService;

	public FMPROG002D0006Controller(
			IFmOrgDutyService dutyService,
			IFmOrgDutyLogicService dutyLogicService) {
		this.dutyService = dutyService;
		this.dutyLogicService = dutyLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmOrgDutyView>>> findPage(@RequestBody SearchBody body) {
		QueryResult<List<FmOrgDutyView>> result = initResult();
		try {
			QueryResult<List<FmOrgDuty>> query = dutyService.findPage(
					queryParameter(body)
							.fullEquals("tenantId")
							.fullEquals("orgUnitId")
							.fullEquals("dutyType")
							.fullEquals("status")
							.fullLink("dutyCodeLike")
							.fullLink("dutyNameLike")
							.value(),
					body.getPageOf().orderBy("TENANT_ID,ORG_UNIT_ID,DUTY_CODE").sortTypeAsc());
			QueryResult<List<FmOrgDutyView>> view = new QueryResult<>();
			view.setValue(query.getValue() == null ? List.of()
					: query.getValue().stream().map(value -> {
						try {
							return dutyLogicService.view(value);
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

	@ControllerMethodAuthority(programId = "FM_PROG002D0006C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> save(
			@RequestBody FmOrgDutyCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> update(
			@RequestBody FmOrgDutyCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> command(
			FmOrgDutyCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmOrgDutyView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("tenantId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
					.testField("orgUnitId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(orgUnitId)", "請選擇部門")
					.testField("dutyCode", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(dutyCode)", "請輸入職務代碼")
					.testField("dutyName", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(dutyName)", "請輸入職務名稱")
					.testField("dutyType", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(dutyType)", "請選擇職務用途")
					.testField("effectiveFrom", command, "effectiveFrom==null", "請輸入生效時間")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(create ? dutyLogicService.create(command)
					: dutyLogicService.update(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgDutyView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					dutyLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgDutyView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(dutyLogicService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006U", check = true)
	@PostMapping("/assignee/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> saveAssignee(
			@RequestBody FmEmployeeDutyCommand command) {
		DefaultControllerJsonResultObj<FmOrgDutyView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("employeeOrgAssignmentId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(employeeOrgAssignmentId)",
							"請選擇擔任人")
					.testField("effectiveFrom", command, "effectiveFrom==null", "請輸入擔任生效時間")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(dutyLogicService.saveAssignee(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006U", check = true)
	@PostMapping("/assignee/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgDutyView>> deactivateAssignee(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgDutyView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					dutyLogicService.deactivateAssignee(body.get("dutyOid"), body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		return options(dutyLogicService.tenantOptions());
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006Q", check = true)
	@PostMapping("/org-unit-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> orgUnitOptions(
			@RequestBody Map<String, String> body) {
		return options(dutyLogicService.orgUnitOptions(body.get("tenantId")));
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0006E", check = true)
	@PostMapping("/assignment-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> assignmentOptions(
			@RequestBody Map<String, String> body) {
		return options(dutyLogicService.assignmentOptions(body.get("dutyOid")));
	}

	private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(
			org.qifu.base.model.DefaultResult<List<FmOptionView>> data) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(data, result);
		return ResponseEntity.ok(result);
	}
}
