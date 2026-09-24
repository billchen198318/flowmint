package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmUserProcessHistoryView(
        String processInstanceId,
        String businessKey,
        String documentNumber,
        String processName,
        String formName,
        String applicantAccount,
        String starterAccount,
        List<String> relations,
        String lastActionType,
        String lastOutcome,
        Date lastActionDate,
        String instanceStatus,
        List<String> currentTaskNames,
        Date startDate,
        Date endDate) {
}
