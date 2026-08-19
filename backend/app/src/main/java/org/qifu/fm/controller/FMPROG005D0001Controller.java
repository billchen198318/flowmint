package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmFormDefCommand;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmFormVersionCommand;
import org.qifu.fm.dto.view.FmFormDefView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.logic.IFmFormDefLogicService;
import org.qifu.fm.service.IFmFormDefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG005D0001")
public class FMPROG005D0001Controller extends CoreApiSupport {

    private static final long serialVersionUID = 1L;

    private final transient IFmFormDefService formDefService;
    private final transient IFmFormDefLogicService formDefLogicService;
    private final transient FmTenantAccessGuard tenantAccessGuard;

    public FMPROG005D0001Controller(
            IFmFormDefService formDefService,
            IFmFormDefLogicService formDefLogicService,
            FmTenantAccessGuard tenantAccessGuard) {
        this.formDefService = formDefService;
        this.formDefLogicService = formDefLogicService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001Q", check = true)
    @PostMapping("/findPage")
    public ResponseEntity<QueryResult<List<FmFormDef>>> findPage(
            @RequestBody SearchBody body) {
        QueryResult<List<FmFormDef>> result = initResult();
        try {
            tenantAccessGuard.requireQueryAccess(
                    body.getField() == null ? null : body.getField().get("tenantId"));
            setQueryResponseJsonResult(formDefService.findPage(
                    queryParameter(body)
                            .fullEquals("tenantId")
                            .fullEquals("status")
                            .fullLink("formCode")
                            .fullLink("formName")
                            .value(),
                    body.getPageOf().orderBy("TENANT_ID,FORM_CODE").sortTypeAsc()),
                    result,
                    body.getPageOf());
        } catch (Exception exception) {
            noSuccessResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001C", check = true)
    @PostMapping("/save")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> save(
            @RequestBody FmFormDefCommand command) {
        return command(command, true);
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001U", check = true)
    @PostMapping("/update")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> update(
            @RequestBody FmFormDefCommand command) {
        return command(command, false);
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> command(
            FmFormDefCommand command,
            boolean create) {
        DefaultControllerJsonResultObj<FmFormDefView> result = initDefaultJsonResult();
        try {
            getCheckControllerFieldHandler(result)
                    .testField("tenantId", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
                            "請選擇 Tenant")
                    .testField("formCode", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(formCode)",
                            "請輸入表單代碼")
                    .testField("formName", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(formName)",
                            "請輸入表單名稱")
                    .throwHtmlMessage();
            setDefaultResponseJsonResult(
                    create ? formDefLogicService.create(command)
                            : formDefLogicService.update(command),
                    result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001E", check = true)
    @PostMapping("/load")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> load(
            @RequestBody Map<String, String> body) {
        return result(() -> formDefLogicService.load(
                body.get("oid"), BaseSystemMessage.dataIsExist()));
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001D", check = true)
    @PostMapping("/deactivate")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> deactivate(
            @RequestBody Map<String, String> body) {
        return result(() -> formDefLogicService.deactivate(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001U", check = true)
    @PostMapping("/version/save-draft")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> saveDraft(
            @RequestBody FmFormVersionCommand command) {
        return result(() -> formDefLogicService.saveDraft(command));
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001U", check = true)
    @PostMapping("/version/create")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> createVersion(
            @RequestBody Map<String, String> body) {
        return result(() -> formDefLogicService.createVersion(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001X", check = true)
    @PostMapping("/version/publish")
    public ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> publish(
            @RequestBody Map<String, String> body) {
        return result(() -> formDefLogicService.publish(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG005D0001Q", check = true)
    @PostMapping("/tenant-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(formDefLogicService.tenantOptions(), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmFormDefView>> result(
            ResultSupplier supplier) {
        DefaultControllerJsonResultObj<FmFormDefView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(supplier.get(), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @FunctionalInterface
    private interface ResultSupplier {
        org.qifu.base.model.DefaultResult<FmFormDefView> get() throws Exception;
    }
}
