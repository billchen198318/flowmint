package org.qifu.fm.controller;

import java.util.List;

import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmIncidentQueryRequest;
import org.qifu.fm.dto.command.FmIncidentReassignRequest;
import org.qifu.fm.dto.command.FmIncidentRetryRequest;
import org.qifu.fm.dto.command.FmProcessTerminateRequest;
import org.qifu.fm.dto.command.FmProcessMonitorRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.dto.view.FmTaskActionResultView;
import org.qifu.fm.dto.view.FmOptionView;
import org.qifu.fm.dto.view.FmProcessMonitorView;
import org.qifu.fm.logic.IFmIncidentOperationsLogicService;
import org.qifu.fm.logic.IFmProcessMonitorLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ResponseBody
@RequestMapping("/api/fm/operations")
public class FmIncidentOperationsController extends CoreApiSupport {

    private final IFmIncidentOperationsLogicService operationsLogicService;
    private final IFmProcessMonitorLogicService processMonitorLogicService;

    public FmIncidentOperationsController(
            IFmIncidentOperationsLogicService operationsLogicService,
            IFmProcessMonitorLogicService processMonitorLogicService) {
        this.operationsLogicService = operationsLogicService;
        this.processMonitorLogicService = processMonitorLogicService;
    }

    @PostMapping("/process-instances")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmProcessMonitorView>>>
            processInstances(
                    @RequestHeader("X-FlowMint-Tenant") String tenantId,
                    @RequestBody(required = false) FmProcessMonitorRequest request) {
        DefaultControllerJsonResultObj<List<FmProcessMonitorView>> result =
                initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(processMonitorLogicService.find(
                    tenantId, request), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/incidents")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmAssignmentIncidentView>>>
            incidents(
                    @RequestHeader("X-FlowMint-Tenant") String tenantId,
                    @RequestBody(required = false) FmIncidentQueryRequest request) {
        DefaultControllerJsonResultObj<List<FmAssignmentIncidentView>> result =
                initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(operationsLogicService.incidents(
                    tenantId, request == null ? null : request.status()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/incidents/reassign-options")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmOptionView>>> reassignOptions(
            @RequestHeader("X-FlowMint-Tenant") String tenantId) {
        DefaultControllerJsonResultObj<List<FmOptionView>> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(operationsLogicService.reassignOptions(
                    tenantId), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/incidents/reassign")
    public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> reassign(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmIncidentReassignRequest request) {
        DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(operationsLogicService.reassign(
                    tenantId, request), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/incidents/retry")
    public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> retry(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmIncidentRetryRequest request) {
        DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(operationsLogicService.retry(
                    tenantId, request), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/process-instances/terminate")
    public ResponseEntity<DefaultControllerJsonResultObj<FmTaskActionResultView>> terminate(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmProcessTerminateRequest request) {
        DefaultControllerJsonResultObj<FmTaskActionResultView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(operationsLogicService.terminate(
                    tenantId, request), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }
}
