package org.qifu.fm.dto.view;

import java.util.Date;

public record FmParallelAddSignMemberView(
        String account,
        String displayName,
        String originalAccount,
        String originalDisplayName,
        String taskId,
        String status,
        String comment,
        Date completedDate) {
}
