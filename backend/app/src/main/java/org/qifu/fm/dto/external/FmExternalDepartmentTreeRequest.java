package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalDepartmentTreeRequest(
		OffsetDateTime effectiveAt,
		String rootOrgUnitId,
		Boolean includeInactive,
		String format,
		Integer page,
		Integer pageSize) {
}
