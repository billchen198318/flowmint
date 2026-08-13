package org.qifu.fm.controller;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultControllerJsonResultObj;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.util.CoreApiSupport;
import org.qifu.fm.dto.command.FmAttachmentSessionFileRequest;
import org.qifu.fm.dto.command.FmAttachmentSessionRequest;
import org.qifu.fm.dto.view.FmAttachmentSessionView;
import org.qifu.fm.dto.view.FmAttachmentUploadView;
import org.qifu.fm.dto.view.FmAttachmentView;
import org.qifu.fm.domain.attachment.FmAttachmentDownloadService;
import org.qifu.fm.logic.IFmAttachmentUploadLogicService;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@ResponseBody
@RequestMapping("/api/fm/attachments")
public class FmAttachmentUploadController extends CoreApiSupport {

    private static final long serialVersionUID = 1L;

    private final transient IFmAttachmentUploadLogicService logicService;
    private final transient FmAttachmentDownloadService downloadService;

    public FmAttachmentUploadController(
            IFmAttachmentUploadLogicService logicService,
            FmAttachmentDownloadService downloadService) {
        this.logicService = logicService;
        this.downloadService = downloadService;
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> download(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @PathVariable String attachmentId) throws ServiceException {
        FmAttachmentDownloadService.DownloadFile file =
                downloadService.download(tenantId, attachmentId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), java.nio.charset.StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.content());
    }

    @GetMapping("/processes/{processInstanceId}")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmAttachmentView>>> listByProcess(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @PathVariable String processInstanceId) {
        DefaultControllerJsonResultObj<List<FmAttachmentView>> result = initDefaultJsonResult();
        try {
            DefaultResult<List<FmAttachmentView>> value = new DefaultResult<>();
            value.setSuccess(org.qifu.base.model.YesNoKeyProvide.YES);
            value.setValue(downloadService.listByProcess(tenantId, processInstanceId));
            setDefaultResponseJsonResult(value, result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmAttachmentView>>> listByTask(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @PathVariable String taskId) {
        DefaultControllerJsonResultObj<List<FmAttachmentView>> result = initDefaultJsonResult();
        try {
            DefaultResult<List<FmAttachmentView>> value = new DefaultResult<>();
            value.setSuccess(org.qifu.base.model.YesNoKeyProvide.YES);
            value.setValue(downloadService.listByTask(tenantId, taskId));
            setDefaultResponseJsonResult(value, result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions")
    public ResponseEntity<DefaultControllerJsonResultObj<FmAttachmentSessionView>> createSession(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmAttachmentSessionRequest request) {
        DefaultControllerJsonResultObj<FmAttachmentSessionView> result = initDefaultJsonResult();
        try {
            if (request == null) throw new ServiceException("Upload Session 參數不可為空");
            setDefaultResponseJsonResult(logicService.createSession(
                    tenantId, request.formId(), request.formVersionNo()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/sessions/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DefaultControllerJsonResultObj<FmAttachmentUploadView>> upload(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestParam String uploadSessionId,
            @RequestParam String fieldKey,
            @RequestParam MultipartFile file) {
        DefaultControllerJsonResultObj<FmAttachmentUploadView> result = initDefaultJsonResult();
        try {
            setDefaultResponseJsonResult(logicService.upload(
                    tenantId, uploadSessionId, fieldKey, file), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions/files/list")
    public ResponseEntity<DefaultControllerJsonResultObj<List<FmAttachmentUploadView>>> list(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmAttachmentSessionFileRequest request) {
        DefaultControllerJsonResultObj<List<FmAttachmentUploadView>> result =
                initDefaultJsonResult();
        try {
            if (request == null) throw new ServiceException("Upload Session 參數不可為空");
            setDefaultResponseJsonResult(logicService.list(
                    tenantId, request.uploadSessionId()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions/files/delete")
    public ResponseEntity<DefaultControllerJsonResultObj<Boolean>> delete(
            @RequestHeader("X-FlowMint-Tenant") String tenantId,
            @RequestBody FmAttachmentSessionFileRequest request) {
        DefaultControllerJsonResultObj<Boolean> result = initDefaultJsonResult();
        try {
            if (request == null) throw new ServiceException("附件刪除參數不可為空");
            setDefaultResponseJsonResult(logicService.delete(
                    tenantId, request.uploadSessionId(), request.attachmentId()), result);
        } catch (Exception exception) {
            exceptionResult(result, exception);
        }
        return ResponseEntity.ok(result);
    }
}
