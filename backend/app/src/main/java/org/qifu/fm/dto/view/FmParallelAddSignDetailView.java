package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmParallelAddSignDetailView(
        String batchOid,
        String parentTaskId,
        String status,
        String initiatorAccount,
        String reason,
        int totalCount,
        int completedCount,
        int agreeCount,
        int disagreeCount,
        Date startedDate,
        Date completedDate,
        Date cancelledDate,
        boolean cancellable,
        List<FmParallelAddSignMemberView> members) {
}
