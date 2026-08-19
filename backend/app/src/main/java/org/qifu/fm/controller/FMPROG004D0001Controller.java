package org.qifu.fm.controller;

import java.util.List;
import java.util.Map;

import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.QueryResult;
import org.qifu.base.model.SearchBody;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmApprovalAuthorityCommand;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.dto.command.FmProcessDefCommand;
import org.qifu.fm.dto.command.FmProcessVersionCommand;
import org.qifu.fm.dto.command.FmResolverPreviewCommand;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmApprovalAuthorityView;
import org.qifu.fm.dto.view.FmProcessDefView;
import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.logic.IFmApprovalAuthorityLogicService;
import org.qifu.fm.logic.IFmProcessDefLogicService;
import org.qifu.fm.service.IFmProcessDefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG004D0001")
public class FMPROG004D0001Controller extends CoreApiSupport {

    private static final long serialVersionUID = 1L;

    private final transient IFmProcessDefService processDefService;
    private final transient IFmProcessDefLogicService processDefLogicService;
    private final transient IFmApprovalAuthorityLogicService approvalAuthorityLogicService;
    private final transient FmTenantAccessGuard tenantAccessGuard;

    public FMPROG004D0001Controller(
            IFmProcessDefService processDefService,
            IFmProcessDefLogicService processDefLogicService,
            IFmApprovalAuthorityLogicService approvalAuthorityLogicService,
            FmTenantAccessGuard tenantAccessGuard) {
        this.processDefService = processDefService;
        this.processDefLogicService = processDefLogicService;
        this.approvalAuthorityLogicService = approvalAuthorityLogicService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/findPage")
    public ResponseEntity<QueryResult<List<FmProcessDef>>> findPage(@RequestBody SearchBody body) {
        QueryResult<List<FmProcessDef>> result = initResult();
        try {
            tenantAccessGuard.requireQueryAccess(
                    body.getField() == null ? null : body.getField().get("tenantId"));
            setQueryResponseJsonResult(processDefService.findPage(
                    queryParameter(body).fullEquals("tenantId").fullEquals("status")
                            .fullLink("processKey").fullLink("processName").value(),
                    body.getPageOf().orderBy("TENANT_ID,PROCESS_KEY").sortTypeAsc()),
                    result, body.getPageOf());
        } catch (Exception exception) {
            noSuccessResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001C", check = true)
    @PostMapping("/save")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> save(
            @RequestBody FmProcessDefCommand command) {
        return command(command, true);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/update")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> update(
            @RequestBody FmProcessDefCommand command) {
        return command(command, false);
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> command(
            FmProcessDefCommand command, boolean create) {
        DefaultControllerJsonResultObj<FmProcessDefView> result = initDefaultJsonResult();
        try {
            getCheckControllerFieldHandler(result)
                    .testField("tenantId", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(tenantId)",
                            "請選擇 Tenant")
                    .testField("processKey", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(processKey)",
                            "請輸入流程代碼")
                    .testField("processName", command,
                            "@org.apache.commons.lang3.StringUtils@isBlank(processName)",
                            "請輸入流程名稱")
                    .throwHtmlMessage();
            setDefaultResponseJsonResult(create ? processDefLogicService.create(command)
                    : processDefLogicService.update(command), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001E", check = true)
    @PostMapping("/load")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> load(
            @RequestBody Map<String, String> body) {
        return result(() -> processDefLogicService.load(
                body.get("oid"), BaseSystemMessage.dataIsExist()));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001D", check = true)
    @PostMapping("/deactivate")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> deactivate(
            @RequestBody Map<String, String> body) {
        return result(() -> processDefLogicService.deactivate(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/version/save-draft")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> saveDraft(
            @RequestBody FmProcessVersionCommand command) {
        return result(() -> processDefLogicService.saveDraft(command));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/version/create")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> createVersion(
            @RequestBody Map<String, String> body) {
        return result(() -> processDefLogicService.createVersion(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001X", check = true)
    @PostMapping("/version/publish")
    public ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> publish(
            @RequestBody Map<String, String> body) {
        return result(() -> processDefLogicService.publish(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/tenant-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> tenantOptions() {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(processDefLogicService.tenantOptions(), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/published-form-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmPublishedFormOptionView>>> publishedFormOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmPublishedFormOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(processDefLogicService.publishedFormOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/resolver-preview")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmResolverPreviewView>>> resolverPreview(
            @RequestBody FmResolverPreviewCommand command) {
        DefaultControllerJsonResultObj<List<FmResolverPreviewView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(processDefLogicService.resolverPreview(command), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/resolver-account-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> resolverAccountOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.resolverAccountOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/org-unit-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> orgUnitOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.orgUnitOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/approval-group-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> approvalGroupOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.approvalGroupOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/approval-level-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> approvalLevelOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.approvalLevelOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/org-title-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> orgTitleOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.orgTitleOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/org-duty-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> orgDutyOptions(
            @RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    processDefLogicService.orgDutyOptions(body.get("tenantId")), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/approval-authority/list")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmApprovalAuthorityView>>>
            approvalAuthorityList(@RequestBody Map<String, String> body) {
        DefaultControllerJsonResultObj<List<FmApprovalAuthorityView>> result =
                initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(
                    approvalAuthorityLogicService.findByProcess(
                            body.get("tenantId"),
                            body.get("processDefId")),
                    result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001Q", check = true)
    @PostMapping("/approval-authority/load")
    public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalAuthorityView>>
            approvalAuthorityLoad(@RequestBody Map<String, String> body) {
        return approvalAuthorityResult(
                () -> approvalAuthorityLogicService.load(body.get("oid")));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/approval-authority/save")
    public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalAuthorityView>>
            approvalAuthoritySave(@RequestBody FmApprovalAuthorityCommand command) {
        return approvalAuthorityResult(() -> approvalAuthorityLogicService.create(command));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/approval-authority/update")
    public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalAuthorityView>>
            approvalAuthorityUpdate(@RequestBody FmApprovalAuthorityCommand command) {
        return approvalAuthorityResult(() -> approvalAuthorityLogicService.update(command));
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/approval-authority/deactivate")
    public ResponseEntity<DefaultControllerJsonResultObj<FmApprovalAuthorityView>>
            approvalAuthorityDeactivate(@RequestBody Map<String, String> body) {
        return approvalAuthorityResult(
                () -> approvalAuthorityLogicService.deactivate(body.get("oid")));
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmApprovalAuthorityView>>
            approvalAuthorityResult(ApprovalAuthorityResultSupplier supplier) {
        DefaultControllerJsonResultObj<FmApprovalAuthorityView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(supplier.get(), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmProcessDefView>> result(
            ResultSupplier supplier) {
        DefaultControllerJsonResultObj<FmProcessDefView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(supplier.get(), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @FunctionalInterface
    private interface ResultSupplier {
        org.qifu.base.model.DefaultResult<FmProcessDefView> get() throws Exception;
    }

    @FunctionalInterface
    private interface ApprovalAuthorityResultSupplier {
        org.qifu.base.model.DefaultResult<FmApprovalAuthorityView> get() throws Exception;
    }
}
