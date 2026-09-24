package org.qifu.fm.controller;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmUserProcessHistoryLoadRequest;
import org.qifu.fm.dto.command.FmUserProcessHistoryRequest;
import org.qifu.fm.dto.view.FmRequestProcessDiagramView;
import org.qifu.fm.dto.view.FmRequestTrackDetailView;
import org.qifu.fm.dto.view.FmUserProcessHistoryPageView;
import org.qifu.fm.logic.IFmUserProcessHistoryLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/FM_PROG007D0004")
public class FMPROG007D0004Controller extends CoreApiSupport {
    private final IFmUserProcessHistoryLogicService logicService;

    public FMPROG007D0004Controller(IFmUserProcessHistoryLogicService logicService) {
        this.logicService = logicService;
    }

    @ControllerMethodAuthority(programId = "FM_PROG007D0004Q", check = true)
    @PostMapping("/findPage")
    public ResponseEntity<DefaultControllerJsonResultObj<FmUserProcessHistoryPageView>> findPage(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody(required = false) FmUserProcessHistoryRequest request) {
        DefaultControllerJsonResultObj<FmUserProcessHistoryPageView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logicService.findPage(tenantId, request), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG007D0004E", check = true)
    @PostMapping("/load")
    public ResponseEntity<DefaultControllerJsonResultObj<FmRequestTrackDetailView>> load(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmUserProcessHistoryLoadRequest request) {
        DefaultControllerJsonResultObj<FmRequestTrackDetailView> result = initDefaultJsonResult();
        try {
            requireRequest(request);
            setDefaultResponseJsonResult(logicService.load(tenantId, request.processInstanceId()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @ControllerMethodAuthority(programId = "FM_PROG007D0004E", check = true)
    @PostMapping("/diagram")
    public ResponseEntity<DefaultControllerJsonResultObj<FmRequestProcessDiagramView>> diagram(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmUserProcessHistoryLoadRequest request) {
        DefaultControllerJsonResultObj<FmRequestProcessDiagramView> result = initDefaultJsonResult();
        try {
            requireRequest(request);
            setDefaultResponseJsonResult(logicService.diagram(tenantId, request.processInstanceId()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    private void requireRequest(FmUserProcessHistoryLoadRequest request) throws ServiceException {
        if (request == null || request.processInstanceId() == null
                || request.processInstanceId().isBlank()) {
            throw new ServiceException("缺少流程實例");
        }
    }
}
