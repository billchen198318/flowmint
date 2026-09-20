package org.qifu.fm.controller;

import org.qifu.base.model.ControllerMethodAuthority;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmGroovyPreviewCommand;
import org.qifu.fm.dto.view.FmGroovyPreviewView;
import org.qifu.fm.logic.IFmGroovyPreviewLogicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/FM_PROG004D0001/groovy")
public class FMGroovyPreviewController extends CoreApiSupport {

    private static final long serialVersionUID = 1L;
    private final transient IFmGroovyPreviewLogicService logic;

    public FMGroovyPreviewController(IFmGroovyPreviewLogicService logic) {
        this.logic = logic;
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/validate")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyPreviewView>> check(
            @RequestBody FmGroovyPreviewCommand command) {
        return execute(command, false);
    }

    @ControllerMethodAuthority(programId = "FM_PROG004D0001U", check = true)
    @PostMapping("/preview")
    public ResponseEntity<DefaultControllerJsonResultObj<FmGroovyPreviewView>> preview(
            @RequestBody FmGroovyPreviewCommand command) {
        return execute(command, true);
    }

    private ResponseEntity<DefaultControllerJsonResultObj<FmGroovyPreviewView>> execute(
            FmGroovyPreviewCommand command, boolean run) {
        DefaultControllerJsonResultObj<FmGroovyPreviewView> result = initDefaultJsonResult();
        try {
            getCheckControllerFieldHandler(result)
                    .testField("oid", command, "@org.apache.commons.lang3.StringUtils@isBlank(oid)", "請選擇流程版本")
                    .testField("expectedLockVersion", command, "expectedLockVersion == null || expectedLockVersion < 0",
                            "缺少草稿版本鎖，請重新載入")
                    .testField("bpmnXml", command, "@org.apache.commons.lang3.StringUtils@isBlank(bpmnXml)", "請提供 BPMN")
                    .testField("nodeId", command, "@org.apache.commons.lang3.StringUtils@isBlank(nodeId)", "請選擇 Groovy 節點")
                    .testField("groovyBindings", command, "groovyBindings == null || groovyBindings.isEmpty()", "缺少腳本設定")
                    .throwHtmlMessage();
            if (run) {
                getCheckControllerFieldHandler(result).testField("sampleInput", command,
                        "@org.apache.commons.lang3.StringUtils@isBlank(sampleInput)", "請輸入人工測試 JSON")
                        .throwHtmlMessage();
            }
            setDefaultResponseJsonResult(run ? logic.preview(command) : logic.check(command), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }
}
