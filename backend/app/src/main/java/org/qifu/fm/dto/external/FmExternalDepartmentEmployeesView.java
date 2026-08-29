package org.qifu.fm.dto.external;

import java.util.Date;
import java.util.List;

public record FmExternalDepartmentEmployeesView(
		FmExternalDepartmentView department,
		long totalElements,
		int totalPages,
		int page,
		int pageSize,
		List<Employee> items) {

	public record Employee(String account, String employeeId, String displayName,
			String orgUnitId, String titleId, String titleName, String approvalLevelId,
			String levelCode, String levelName, Integer levelOrder, String primaryFlag,
			Date effectiveFrom, Date effectiveTo) { }
}
