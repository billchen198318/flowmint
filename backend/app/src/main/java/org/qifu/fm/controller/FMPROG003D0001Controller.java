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
import org.qifu.fm.dto.command.FmApprovalGroupCommand;
import org.qifu.fm.dto.command.FmApprovalGroupMemberCommand;
import org.qifu.fm.dto.view.FmApprovalGroupView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmApprovalGroup;
import org.qifu.fm.logic.IFmApprovalGroupLogicService;
import org.qifu.fm.service.IFmApprovalGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG003D0001")
public class FMPROG003D0001Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;

	private final transient IFmApprovalGroupService groupService;
	private final transient IFmApprovalGroupLogicService groupLogicService;

	public FMPROG003D0001Controller(
			IFmApprovalGroupService groupService,
			IFmApprovalGroupLogicService groupLogicService) {
		this.groupService = groupService;
		this.groupLogicService = groupLogicService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001Q", check = true)
	@PostMapping("/findPage")
	public ResponseEntity<QueryResult<List<FmApprovalGroupView>>> findPage(
			@RequestBody SearchBody body) {
		QueryResult<List<FmApprovalGroupView>> result = initResult();
		try {
			QueryResult<List<FmApprovalGroup>> query = groupService.findPage(
					queryParameter(body)
							.fullEquals("tenantId")
							.fullEquals("assignmentMode")
							.fullEquals("status")
							.fullLink("groupCodeLike")
							.fullLink("groupNameLike")
							.value(),
					body.getPageOf().orderBy("TENANT_ID,GROUP_CODE").sortTypeAsc());
			QueryResult<List<FmApprovalGroupView>> view = new QueryResult<>();
			view.setValue(query.getValue() == null ? List.of()
					: query.getValue().stream().map(value -> {
						try {
							return groupLogicService.view(value);
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

	@ControllerMethodAuthority(programId = "FM_PROG003D0001C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> save(
			@RequestBody FmApprovalGroupCommand command) {
		return command(command, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> update(
			@RequestBody FmApprovalGroupCommand command) {
		return command(command, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> command(
			FmApprovalGroupCommand command,
			boolean create) {
		DefaultControllerJsonResultObj<FmApprovalGroupView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("tenantId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
							"請選擇 Tenant")
					.testField("groupCode", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(groupCode)",
							"請輸入群組代碼")
					.testField("groupName", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(groupName)",
							"請輸入群組名稱")
					.testField("assignmentMode", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(assignmentMode)",
							"請選擇處理方式")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(create ? groupLogicService.create(command)
					: groupLogicService.update(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> load(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmApprovalGroupView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					groupLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> deactivate(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmApprovalGroupView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(groupLogicService.deactivate(body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001U", check = true)
	@PostMapping("/member/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> saveMember(
			@RequestBody FmApprovalGroupMemberCommand command) {
		DefaultControllerJsonResultObj<FmApprovalGroupView> result = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(result)
					.testField("employeeId", command,
							"@org.apache.commons.lang3.StringUtils@isBlank(employeeId)",
							"請選擇群組成員")
					.testField("effectiveFrom", command, "effectiveFrom==null", "請輸入生效時間")
					.throwHtmlMessage();
			setDefaultResponseJsonResult(groupLogicService.saveMember(command), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001U", check = true)
	@PostMapping("/member/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalGroupView>> deactivateMember(
			@RequestBody Map<String, String> body) {
		DefaultControllerJsonResultObj<FmApprovalGroupView> result = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(
					groupLogicService.deactivateMember(body.get("groupOid"), body.get("oid")), result);
		} catch (Exception exception) {
			exceptionResult(result, exception);
		}
		return ResponseEntity.ok(result);
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
		return options(groupLogicService.tenantOptions());
	}

	@ControllerMethodAuthority(programId = "FM_PROG003D0001E", check = true)
	@PostMapping("/employee-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> employeeOptions(
			@RequestBody Map<String, String> body) {
		return options(groupLogicService.employeeOptions(body.get("groupOid")));
	}

	private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(
			DefaultResult<List<FmOptionView>> data) {
		DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
		setDefaultResponseJsonResult(data, result);
		return ResponseEntity.ok(result);
	}
}
