package org.qifu.fm.controller;

import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmGroovyIncidentHandleCommand;
import org.qifu.fm.dto.view.FmGroovyIncidentHandlingView;
import org.qifu.fm.logic.IFmGroovyIncidentHandlingLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/operations/system-tasks/handling")
public class FmGroovyIncidentHandlingController extends CoreApiSupport {

    private final IFmGroovyIncidentHandlingLogicService logic;

    public FmGroovyIncidentHandlingController(IFmGroovyIncidentHandlingLogicService logic) {
        this.logic = logic;
    }

    @PostMapping("/detail")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyIncidentHandlingView>> detail(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody Detail request) {
        DefaultControllerJsonResultObj<FmGroovyIncidentHandlingView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.load(tenantId,
                    request == null ? null : request.invocationId(), request == null ? 0 : request.offset()), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/handle")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyIncidentHandlingView>> handle(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody FmGroovyIncidentHandleCommand command) {
        DefaultControllerJsonResultObj<FmGroovyIncidentHandlingView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.handle(tenantId, command), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    public record Detail(String invocationId, int offset) {
    }
}
