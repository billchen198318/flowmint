package org.qifu.fm.dto.view;

import java.util.List;

public record FmOrganizationHealthIssueView(
		String section,
		String code,
		String severity,
		String title,
		String subjectId,
		String subjectLabel,
		String description,
		String impact,
		String evidence,
		String recommendation,
		String targetProgramId,
		List<String> path) {
}
