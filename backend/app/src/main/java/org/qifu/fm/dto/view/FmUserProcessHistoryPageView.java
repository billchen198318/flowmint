package org.qifu.fm.dto.view;

import java.util.List;

public record FmUserProcessHistoryPageView(
        List<FmUserProcessHistoryView> items,
        Long totalCount,
        Integer totalPages,
        Integer page,
        Integer pageSize) {
}
