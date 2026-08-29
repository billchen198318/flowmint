package org.qifu.fm.dto.external;

import java.util.Date;
import java.util.List;

public record FmExternalEmployeeDepartmentsView(
		Employee employee,
		List<Assignment> assignments) {

	public record Employee(String account, String employeeId, String displayName) { }

	public record Assignment(
		String assignmentId,
		FmExternalDepartmentView department,
		String titleId,
		String primaryFlag,
		Date effectiveFrom,
		Date effectiveTo) { }
}
