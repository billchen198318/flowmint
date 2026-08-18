package org.qifu.fm.dto.view;

public record FmAttachmentUploadView(
        String attachmentId,
        String fieldKey,
        String fileName,
        String contentType,
        Long fileSize,
        String scanStatus) {
}
