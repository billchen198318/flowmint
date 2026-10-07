package org.qifu.fm.controller;

import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmOrgTitleCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmOrgTitleView;
import org.qifu.fm.domain.organization.FmApprovalLevelAvailability;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmOrgTitle;
import org.qifu.fm.logic.IFmOrgTitleLogicService;
import org.qifu.fm.service.IFmOrgTitleService;
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
@RequestMapping("/api/FM_PROG002D0004")
public class FMPROG002D0004Controller extends CoreApiSupport {
  private static final long serialVersionUID = 1L;
  private final transient IFmOrgTitleService titleService;
  private final transient IFmOrgTitleLogicService titleLogicService;
  private final transient IFmTenantService tenantService;
  private final transient FmApprovalLevelAvailability availableLevels;
  private final transient FmTenantAccessGuard tenantAccessGuard;

  public FMPROG002D0004Controller(IFmOrgTitleService titleService,
      IFmOrgTitleLogicService titleLogicService, IFmTenantService tenantService,
      FmApprovalLevelAvailability availableLevels, FmTenantAccessGuard tenantAccessGuard) {
    this.titleService = titleService;
    this.titleLogicService = titleLogicService;
    this.tenantService = tenantService;
    this.availableLevels = availableLevels;
    this.tenantAccessGuard = tenantAccessGuard;
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004Q", check = true)
  @PostMapping(value = "/findPage", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<QueryResult<List<FmOrgTitleView>>> findPage(@RequestBody SearchBody body) {
    QueryResult<List<FmOrgTitleView>> result = initResult();
    try {
      Map<String, Object> parameters = queryParameter(body)
          .fullEquals("tenantId").fullEquals("approvalLevelId")
          .fullEquals("status").fullLink("titleCodeLike").fullLink("titleNameLike").value();
      String tenantId = (String) parameters.get("tenantId");
      if (tenantId != null && !tenantId.isBlank())
        tenantAccessGuard.requireAccess(tenantId);
      parameters.put("readableTenantIds", tenantAccessGuard.readableTenantIds());
      QueryResult<List<FmOrgTitle>> query = titleService.findPage(parameters,
          body.getPageOf().orderBy("TENANT_ID,SORT_NO,TITLE_CODE").sortTypeAsc());
      QueryResult<List<FmOrgTitleView>> view = new QueryResult<>();
      view.setValue(query.getValue() == null ? List.of()
          : query.getValue().stream().map(FmOrgTitleView::from).toList());
      view.setMessage(query.getMessage());
      setQueryResponseJsonResult(view, result, body.getPageOf());
    } catch (Exception exception) {
      noSuccessResult(result, exception);
    }
    return ResponseEntity.ok(result);
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004C", check = true)
  @PostMapping("/save")
  public ResponseEntity<DefaultControllerJsonResultObj<FmOrgTitleView>> save(
      @RequestBody FmOrgTitleCommand command) { return command(command, true); }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004U", check = true)
  @PostMapping("/update")
  public ResponseEntity<DefaultControllerJsonResultObj<FmOrgTitleView>> update(
      @RequestBody FmOrgTitleCommand command) { return command(command, false); }

  private ResponseEntity<DefaultControllerJsonResultObj<FmOrgTitleView>> command(
      FmOrgTitleCommand command, boolean create) {
    DefaultControllerJsonResultObj<FmOrgTitleView> result = initDefaultJsonResult();
    try {
      getCheckControllerFieldHandler(result)
          .testField("tenantId", command, "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)", "請選擇 Tenant")
          .testField("titleCode", command, "@org.apache.commons.lang3.StringUtils@isBlank(titleCode)", "請輸入職稱代碼")
          .testField("titleName", command, "@org.apache.commons.lang3.StringUtils@isBlank(titleName)", "請輸入職稱名稱")
          .testField("approvalLevelId", command, "@org.apache.commons.lang3.StringUtils@isBlank(approvalLevelId)", "請選擇簽核 Level")
          .testField("effectiveFrom", command, "effectiveFrom==null", "請輸入生效時間").throwHtmlMessage();
      setDefaultResponseJsonResult(create ? titleLogicService.create(command)
          : titleLogicService.update(command), result);
    } catch (Exception exception) { exceptionResult(result, exception); }
    return ResponseEntity.ok(result);
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004E", check = true)
  @PostMapping("/load")
  public ResponseEntity<DefaultControllerJsonResultObj<FmOrgTitleView>> load(
      @RequestBody Map<String, String> body) {
    DefaultControllerJsonResultObj<FmOrgTitleView> result = initDefaultJsonResult();
    try { setDefaultResponseJsonResult(titleLogicService.load(body.get("oid"), BaseSystemMessage.dataIsExist()), result); }
    catch (Exception exception) { exceptionResult(result, exception); }
    return ResponseEntity.ok(result);
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004D", check = true)
  @PostMapping("/deactivate")
  public ResponseEntity<DefaultControllerJsonResultObj<FmOrgTitleView>> deactivate(
      @RequestBody Map<String, String> body) {
    DefaultControllerJsonResultObj<FmOrgTitleView> result = initDefaultJsonResult();
    try { setDefaultResponseJsonResult(titleLogicService.deactivate(body.get("oid")), result); }
    catch (Exception exception) { exceptionResult(result, exception); }
    return ResponseEntity.ok(result);
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004Q", check = true)
  @PostMapping("/tenant-options")
  public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("status", "ACTIVE");
      var readableTenantIds = tenantAccessGuard.readableTenantIds();
      return options(tenantService.selectListByParams(params, "TENANT_CODE", "ASC").getValue().stream()
          .filter(value -> readableTenantIds.contains(value.getTenantId()))
          .map(value -> new FmOptionView(value.getTenantId(), value.getTenantCode() + "／" + value.getTenantName())).toList());
    } catch (Exception exception) { return optionError(exception); }
  }

  @ControllerMethodAuthority(programId = "FM_PROG002D0004Q", check = true)
  @PostMapping("/level-options")
  public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> levelOptions(
      @RequestBody Map<String, String> body) {
    try {
      String tenantId = body.get("tenantId");
      tenantAccessGuard.requireAccess(tenantId);
      Date now = new Date();
      return options(availableLevels.availableDuring(tenantId, now, now).stream()
          .map(value -> new FmOptionView(value.getApprovalLevelId(),
              value.getLevelCode() + "／" + value.getLevelName())).toList());
    } catch (Exception exception) { return optionError(exception); }
  }

  private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> optionError(Exception exception) {
    DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
    exceptionResult(result, exception);
    return ResponseEntity.ok(result);
  }

  private ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> options(List<FmOptionView> values) {
    DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
    DefaultResult<List<FmOptionView>> data = new DefaultResult<>();
    data.setSuccess("Y"); data.setValue(values);
    setDefaultResponseJsonResult(data, result);
    return ResponseEntity.ok(result);
  }
}
