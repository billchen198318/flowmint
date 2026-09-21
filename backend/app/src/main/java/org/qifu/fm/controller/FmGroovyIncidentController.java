package org.qifu.fm.controller;

import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.view.FmGroovyIncidentPageView;
import org.qifu.fm.dto.view.FmGroovyIncidentView;
import org.qifu.fm.logic.IFmGroovyIncidentLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/operations/system-tasks")
public class FmGroovyIncidentController extends CoreApiSupport {

    private final IFmGroovyIncidentLogicService logic;

    public FmGroovyIncidentController(IFmGroovyIncidentLogicService logic) {
        this.logic = logic;
    }

    @PostMapping("/incidents")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyIncidentPageView>> find(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody(required = false) Query request) {
        DefaultControllerJsonResultObj<FmGroovyIncidentPageView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.find(tenantId, request == null ? 0 : request.offset()), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/detail")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyIncidentView>> detail(
            @RequestHeader("X-FlowMint-Tenant") String tenantId, @RequestBody Detail request) {
        DefaultControllerJsonResultObj<FmGroovyIncidentView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logic.detail(tenantId, request == null ? null : request.invocationId()), result);
        } catch (Exception failure) {
            exceptionResult(result, failure);
        }
        return ResponseEntity.ok(result);
    }

    public record Query(int offset) {
    }

    public record Detail(String invocationId) {
    }
}
