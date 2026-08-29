package org.qifu.fm.dto.external;

public record FmExternalEmployeeManagerView(
		FmExternalEmployeeDepartmentsView.Employee employee,
		FmExternalEmployeeDepartmentsView.Assignment assignment,
		Manager manager,
		String warning) {

	public record Manager(String account, String employeeId, String displayName,
			String assignmentId, String source) { }
}
