package org.qifu.fm.domain.externalapi;

import java.util.Map;

public final class FmExternalApiEndpointCatalog {
	private static final Map<String, String> SCOPES = Map.ofEntries(
			Map.entry("org/departments/detail", "org.department.read"),
			Map.entry("org/departments/primary-head", "org.department.read"),
			Map.entry("org/departments/employees", "org.department.read"),
			Map.entry("org/departments/tree", "org.department.read"),
			Map.entry("org/employees/detail", "org.employee.read"),
			Map.entry("org/employees/approval-level", "org.employee.read"),
			Map.entry("org/employees/direct-manager", "org.employee.read"),
			Map.entry("org/employees/departments", "org.employee.read"),
			Map.entry("org/employees/primary-department-head", "org.employee.read"),
			Map.entry("org/employees/parent-department", "org.employee.read"),
			Map.entry("design/processes/forms", "design.process.read"),
			Map.entry("design/forms/template", "design.form.read"),
			Map.entry("runtime/requests/submit", "runtime.request.submit"),
			Map.entry("runtime/requests/status", "runtime.request.read"));
	private FmExternalApiEndpointCatalog() { }
	public static String requiredScope(String endpointCode) {
		return SCOPES.get(endpointCode);
	}
}
