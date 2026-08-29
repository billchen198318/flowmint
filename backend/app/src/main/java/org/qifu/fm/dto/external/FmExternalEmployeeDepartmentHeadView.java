package org.qifu.fm.dto.external;

public record FmExternalEmployeeDepartmentHeadView(
		FmExternalEmployeeDepartmentsView.Employee employee,
		FmExternalDepartmentView primaryDepartment,
		FmExternalDepartmentHeadView.Head head,
		String warning) {
}
