package org.qifu.fm.dto.external;

import java.util.Date;

public record FmExternalEmployeeView(
		String account,
		String employeeId,
		String employeeNo,
		String displayName,
		String employmentStatus,
		Date effectiveFrom,
		Date effectiveTo,
		String companyEmail,
		FmExternalDepartmentView primaryDepartment) { }
