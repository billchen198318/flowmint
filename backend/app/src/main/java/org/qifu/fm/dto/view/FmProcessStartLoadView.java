package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessStartLoadView(
		String processDefId,
		Integer processVersionNo,
		String processKey,
		String processName,
		String applicantAccount,
		List<FmProcessStartFormView> forms) {
}
