package org.qifu.fm.dto.external;

import java.util.Date;

public record FmExternalDepartmentHeadView(
		FmExternalDepartmentView department,
		Head head,
		String warning) {
	public record Head(String account, String employeeId, String displayName,
			String headType, Date effectiveFrom, Date effectiveTo) { }
}
