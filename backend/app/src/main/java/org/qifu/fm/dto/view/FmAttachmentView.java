package org.qifu.fm.dto.view;

public record FmAttachmentView(
        String attachmentId,
        String fieldKey,
        String fileName,
        String contentType,
        Long fileSize) {
}
