package org.qifu.fm.controller;

import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmGroovyRetryCommand;
import org.qifu.fm.dto.view.FmGroovyRetryView;
import org.qifu.fm.logic.IFmGroovyRetryLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/operations/system-tasks")
public class FmGroovyRetryController extends CoreApiSupport {
    private final IFmGroovyRetryLogicService logic;

    public FmGroovyRetryController(IFmGroovyRetryLogicService logic) {
        this.logic = logic;
    }

    @PostMapping("/retry/preview")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyRetryView>> preview(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody Preview request) {
        DefaultControllerJsonResultObj<FmGroovyRetryView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.preview(tenantId, request == null ? null : request.invocationId()), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/retry")
    public ResponseEntity<DefaultControllerJsonResultObj<Boolean>> retry(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody FmGroovyRetryCommand command) {
        DefaultControllerJsonResultObj<Boolean> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.retry(tenantId, command), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    public record Preview(String invocationId) {
    }
}
