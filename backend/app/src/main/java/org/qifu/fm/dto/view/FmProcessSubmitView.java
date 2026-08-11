package org.qifu.fm.dto.view;

public record FmProcessSubmitView(
        String businessKey,
        String formDataId,
        String processInstanceId,
        String instanceStatus) {
}
