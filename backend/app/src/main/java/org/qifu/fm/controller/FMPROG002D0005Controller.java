package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmOrgUnitHeadCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgUnitHeadView;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.logic.IFmOrgUnitHeadLogicService;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG002D0005")
public class FMPROG002D0005Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmOrgUnitHeadService headService;
	private final transient IFmOrgUnitHeadLogicService headLogicService;

	public FMPROG002D0005Controller(IFmOrgUnitHeadService headService,
			IFmOrgUnitHeadLogicService headLogicService) {
		this.headService = headService;
		this.headLogicService = headLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmOrgUnitHeadView>>> findPage(@RequestBody SearchBody body) {
		QueryResult<List<FmOrgUnitHeadView>> result = initResult();
		try {
			QueryResult<List<FmOrgUnitHead>> query = headService.findPage(queryParameter(body)
					.fullEquals("tenantId").fullEquals("orgUnitId").fullEquals("headType")
					.fullEquals("status").value(),
					body.getPageOf().orderBy("TENANT_ID,ORG_UNIT_ID,HEAD_TYPE,PRIORITY").sortTypeAsc());
			QueryResult<List<FmOrgUnitHeadView>> view = new QueryResult<>();
			view.setValue(query.getValue() == null ? List.of()
					: query.getValue().stream().map(value -> {
						try { return headLogicService.view(value); }
						catch (Exception exception) { throw new IllegalStateException(exception); }
					}).toList());
			view.setMessage(query.getMessage());
			setQueryResponseJsonResult(view, result, body.getPageOf());
		} catch (Exception exception) { noSuccessResult(result, exception); }
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitHeadView>> save(
			@RequestBody FmOrgUnitHeadCommand command) { return command(command, true); }

	@ControllerMethodAuthority(programId = "FM_PROG002D0005U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitHeadView>> update(
			@RequestBody FmOrgUnitHeadCommand command) { return command(command, false); }

	private ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitHeadView>> command(
			FmOrgUnitHeadCommand command, boolean create) {
		DefaultControllerJsonResultObj<FmOrgUnitHeadView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("tenantId", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
					.testField("orgUnitId", command, "@org.apache.commons.lang3.StringUtils@isBlank(orgUnitId)", "請選擇部門")
					.testField("employeeId", command, "@org.apache.commons.lang3.StringUtils@isBlank(employeeId)", "請選擇主管員工")
					.testField("headType", command, "@org.apache.commons.lang3.StringUtils@isBlank(headType)", "請選擇主管類型")
					.testField("effectiveFrom", command, "effectiveFrom==null", "請輸入生效時間")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(create ? headLogicService.create(command)
					: headLogicService.update(command), result);
		} catch (Exception exception) { exceptionResult(result, exception); }
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitHeadView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgUnitHeadView> result = initDefaultJsonResult();
		try { setDefaultResponseJsonResult(headLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result); }
		catch (Exception exception) { exceptionResult(result, exception); }
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgUnitHeadView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmOrgUnitHeadView> result = initDefaultJsonResult();
		try { setDefaultResponseJsonResult(headLogicService.deactivate(body.get("oid")), result); }
		catch (Exception exception) { exceptionResult(result, exception); }
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		return options(headLogicService.tenantOptions());
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005Q", check = true)
	@PostMapping("/org-unit-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> orgUnitOptions(
			@RequestBody Map<String, String> body) {
		return options(headLogicService.orgUnitOptions(body.get("tenantId")));
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0005Q", check = true)
	@PostMapping("/employee-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> employeeOptions(
			@RequestBody Map<String, String> body) {
		return options(headLogicService.employeeOptions(body.get("tenantId"), body.get("orgUnitId")));
	}

	private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(
			org.qifu.base.model.DefaultResult<List<FmOptionView>> data) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(data, result);
		return ResponseEntity.ok(result);
	}
}
