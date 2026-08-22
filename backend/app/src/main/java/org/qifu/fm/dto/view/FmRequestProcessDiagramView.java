package org.qifu.fm.dto.view;

import java.util.List;

public record FmRequestProcessDiagramView(
        String bpmnXml,
        String processStatus,
        List<String> activeActivityIds,
        List<String> completedActivityIds) {
}
