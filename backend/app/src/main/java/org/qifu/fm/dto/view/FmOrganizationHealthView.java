package org.qifu.fm.dto.view;

import java.util.Date;
import java.util.List;

public record FmOrganizationHealthView(
		String tenantId,
		Date checkedAt,
		List<FmOrganizationHealthIssueView> headIssues,
		List<FmOrganizationHealthIssueView> managerIssues,
		List<FmOrganizationHealthIssueView> organizationIssues,
		List<FmOrganizationHealthIssueView> delegationIssues) {
}
