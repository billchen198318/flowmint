package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalEmployeeOrganizationRequest(
		String account,
		OffsetDateTime effectiveAt,
		String orgUnitId,
		Boolean primaryOnly,
		Boolean includeAncestors,
		Boolean fallbackToOrgHead) {
}
