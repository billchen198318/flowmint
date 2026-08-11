package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmProcessMonitorView(
		String processInstanceId,
		String businessKey,
		String processName,
		Integer processVersionNo,
		String ownerAccount,
		String initiatorAccount,
		String instanceStatus,
		List<String> currentTaskNames,
		Date nearestDueDate,
		Integer overdueTaskCount,
		Long elapsedMinutes,
		Date startDate,
		Date endDate) {
}
