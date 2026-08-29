package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalDepartmentEmployeesRequest(
		String orgUnitId,
		OffsetDateTime effectiveAt,
		Boolean includeSubtree,
		Boolean primaryOnly,
		Integer page,
		Integer pageSize,
		String status) {
}
