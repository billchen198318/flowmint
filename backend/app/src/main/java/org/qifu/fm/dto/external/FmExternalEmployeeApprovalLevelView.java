package org.qifu.fm.dto.external;

public record FmExternalEmployeeApprovalLevelView(
		FmExternalEmployeeDepartmentsView.Employee employee,
		FmExternalEmployeeDepartmentsView.Assignment assignment,
		Level level) {

	public record Level(String schemeId, String approvalLevelId, String levelCode,
			String levelName, Integer levelOrder) { }
}
