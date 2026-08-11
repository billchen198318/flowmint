package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessMonitorPageView(
        List<FmProcessMonitorView> items,
        Long totalCount,
        Integer totalPages,
        Integer page,
        Integer pageSize) {
}
