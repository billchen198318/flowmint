package org.qifu.fm.dto.view;

import java.util.List;

public record FmProcessStartFormView(
		String formId,
		Integer formVersionNo,
		String formCode,
		String formName,
		String schemaContent,
		String uiSchemaContent,
		String customScriptContent,
		List<String> taskDefKeys) {
}
