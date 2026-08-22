package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmCurrentApprovalView(
        String taskId,
        String taskDefKey,
        String taskName,
        String assignmentType,
        List<FmCurrentApprovalPersonView> approvers,
        Date createDate) {
}
