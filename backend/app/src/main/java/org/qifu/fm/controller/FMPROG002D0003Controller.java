package org.qifu.fm.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmOrgLevelSchemeCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgLevelSchemeView;
import org.qifu.fm.entity.FmOrgLevelScheme;
import org.qifu.fm.logic.IFmOrgLevelSchemeLogicService;
import org.qifu.fm.service.IFmOrgLevelSchemeService;
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
@RequestMapping("/api/FM_PROG002D0003")
public class FMPROG002D0003Controller extends CoreApiSupport {

	private static final long serialVersionUID = 1L;
	private final transient IFmOrgLevelSchemeService fmOrgLevelSchemeService;
	private final transient IFmOrgLevelSchemeLogicService fmOrgLevelSchemeLogicService;
	private final transient IFmTenantService fmTenantService;

	public FMPROG002D0003Controller(IFmOrgLevelSchemeService fmOrgLevelSchemeService, IFmOrgLevelSchemeLogicService fmOrgLevelSchemeLogicService, IFmTenantService fmTenantService) {
		this.fmOrgLevelSchemeService = fmOrgLevelSchemeService;
		this.fmOrgLevelSchemeLogicService = fmOrgLevelSchemeLogicService;
		this.fmTenantService = fmTenantService;
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003Q", check = true)
	@PostMapping(value = "/findPage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<QueryResult<List<FmOrgLevelSchemeView>>> findPage(@RequestBody SearchBody b) {
		QueryResult<List<FmOrgLevelSchemeView>> r = initResult();
		try {
			QueryResult<List<FmOrgLevelScheme>> q = fmOrgLevelSchemeService.findPage(
					queryParameter(b).fullEquals("tenantId").fullEquals("status").fullLink("schemeCodeLike")
							.fullLink("schemeNameLike").value(),
					b.getPageOf().orderBy("TENANT_ID,SCHEME_CODE").sortTypeAsc());
			QueryResult<List<FmOrgLevelSchemeView>> v = new QueryResult<>();
			v.setValue(q.getValue() == null ? List.of()
					: q.getValue().stream().map(x -> FmOrgLevelSchemeView.from(x, List.of())).toList());
			v.setMessage(q.getMessage());
			setQueryResponseJsonResult(v, r, b.getPageOf());
		} catch (Exception e) {
			noSuccessResult(r, e);
		}
		return ResponseEntity.ok(r);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003C", check = true)
	@PostMapping("/save")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgLevelSchemeView>> save(
			@RequestBody FmOrgLevelSchemeCommand c) {
		return command(c, true);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003U", check = true)
	@PostMapping("/update")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgLevelSchemeView>> update(
			@RequestBody FmOrgLevelSchemeCommand c) {
		return command(c, false);
	}

	private ResponseEntity<DefaultControllerJsonResultObj<FmOrgLevelSchemeView>> command(FmOrgLevelSchemeCommand c,
			boolean create) {
		DefaultControllerJsonResultObj<FmOrgLevelSchemeView> r = initDefaultJsonResult();
		try {
			getCheckControllerFieldHandler(r)
					.testField("tenantId", c, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
					.testField("schemeCode", c, "@org.apache.commons.lang3.StringUtils@isBlank(schemeCode)", "請輸入方案代碼")
					.testField("schemeName", c, "@org.apache.commons.lang3.StringUtils@isBlank(schemeName)", "請輸入方案名稱")
					.testField("effectiveFrom", c, "effectiveFrom==null", "請輸入生效時間").throwHtmlMessage();
			setDefaultResponseJsonResult(create ? fmOrgLevelSchemeLogicService.create(c) : fmOrgLevelSchemeLogicService.update(c), r);
		} catch (Exception e) {
			exceptionResult(r, e);
		}
		return ResponseEntity.ok(r);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003E", check = true)
	@PostMapping("/load")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgLevelSchemeView>> load(
			@RequestBody Map<String, String> b) {
		DefaultControllerJsonResultObj<FmOrgLevelSchemeView> r = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(fmOrgLevelSchemeLogicService.load(b.get("oid"), BaseSystemMessage.dataIsExist()), r);
		} catch (Exception e) {
			exceptionResult(r, e);
		}
		return ResponseEntity.ok(r);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003D", check = true)
	@PostMapping("/deactivate")
	public ResponseEntity<DefaultControllerJsonResultObj<FmOrgLevelSchemeView>> deactivate(
			@RequestBody Map<String, String> b) {
		DefaultControllerJsonResultObj<FmOrgLevelSchemeView> r = initDefaultJsonResult();
		try {
			setDefaultResponseJsonResult(fmOrgLevelSchemeLogicService.deactivate(b.get("oid")), r);
		} catch (Exception e) {
			exceptionResult(r, e);
		}
		return ResponseEntity.ok(r);
	}

	@ControllerMethodAuthority(programId = "FM_PROG002D0003Q", check = true)
	@PostMapping("/tenant-options")
	public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options() {
		DefaultControllerJsonResultObj<List<FmOptionView>> r = initDefaultJsonResult();
		try {
			Map<String, Object> p = new HashMap<>();
			p.put("status", "ACTIVE");
			DefaultResult<List<FmOptionView>> d = new DefaultResult<>();
			d.setSuccess("Y");
			d.setValue(fmTenantService.selectListByParams(p, "TENANT_CODE", "ASC").getValue().stream()
					.map(x -> new FmOptionView(x.getTenantId(), x.getTenantCode() + "／" + x.getTenantName())).toList());
			setDefaultResponseJsonResult(d, r);
		} catch (Exception e) {
			exceptionResult(r, e);
		}
		return ResponseEntity.ok(r);
	}
}
