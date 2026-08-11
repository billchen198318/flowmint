package org.qifu.fm.dto.view;

import java.util.Date;

public record FmTaskInboxView(
        String taskId,
        String taskDefKey,
        String taskName,
        String processInstanceId,
        String businessKey,
        String processName,
        String applicantAccount,
        Date createdDate,
        Date dueDate) {
}
