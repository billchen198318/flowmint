package org.qifu.fm.dto.view;

import java.util.Date;

public record FmTaskHistoryView(
        String taskDefKey,
        String taskName,
        String assignee,
        Date startDate,
        Date endDate) {
}
