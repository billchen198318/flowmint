package org.qifu.fm.dto.external;

import java.util.Date;
import java.util.List;

public record FmExternalProcessFormsView(
		String processDefId,
		String processName,
		Integer versionNo,
		String deploymentStatus,
		Date publishedAt,
		List<FormBinding> forms) {

	public record FormBinding(String formId, String formName, Integer formVersionNo,
			String schemaHash, List<String> bindingUsage, List<String> taskDefinitionKeys,
			List<FieldPolicySummary> fieldPolicies) { }

	public record FieldPolicySummary(String taskDefinitionKey, String defaultAccess,
			int overrideCount) { }
}
