package org.qifu.fm.dto.external;

import java.time.OffsetDateTime;

public record FmExternalDepartmentDetailRequest(
		String orgUnitId,
		OffsetDateTime effectiveAt) {
}
