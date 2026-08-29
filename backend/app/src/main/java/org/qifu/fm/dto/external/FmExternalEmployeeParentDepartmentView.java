package org.qifu.fm.dto.external;

import java.util.List;

public record FmExternalEmployeeParentDepartmentView(
		FmExternalEmployeeDepartmentsView.Employee employee,
		FmExternalDepartmentView primaryDepartment,
		FmExternalDepartmentView parentDepartment,
		List<FmExternalDepartmentView> ancestors) {
}
