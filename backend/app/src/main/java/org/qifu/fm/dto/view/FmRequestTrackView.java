package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmRequestTrackView(
        String processInstanceId,
        String businessKey,
        String documentNumber,
        String processName,
        String formName,
        String applicantAccount,
        String starterAccount,
        String instanceStatus,
        Date startDate,
        Date endDate,
        List<String> currentTaskNames) {
}
