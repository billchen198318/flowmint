package org.qifu.fm.controller;

import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmGroovyRecalculateCommand;
import org.qifu.fm.dto.view.FmGroovyRecalculateView;
import org.qifu.fm.logic.IFmGroovyRecalculateLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/operations/system-tasks")
public class FmGroovyRecalculateController extends CoreApiSupport {
    private final IFmGroovyRecalculateLogicService logic;

    public FmGroovyRecalculateController(IFmGroovyRecalculateLogicService logic) {
        this.logic = logic;
    }

    @PostMapping("/recalculate/preview")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyRecalculateView>> preview(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody Preview request) {
        DefaultControllerJsonResultObj<FmGroovyRecalculateView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.preview(tenantId, request == null ? null : request.invocationId()), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<DefaultControllerJsonResultObj<String>> recalculate(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody FmGroovyRecalculateCommand command) {
        DefaultControllerJsonResultObj<String> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.recalculate(tenantId, command), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    public record Preview(String invocationId) {
    }
}
