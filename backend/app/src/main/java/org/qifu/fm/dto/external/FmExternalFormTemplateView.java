package org.qifu.fm.dto.external;

import java.util.Date;
import java.util.List;

import tools.jackson.databind.JsonNode;

public record FmExternalFormTemplateView(
		String formId,
		String formName,
		Integer versionNo,
		String schemaType,
		String schemaHash,
		JsonNode templateJson,
		List<FieldContract> submissionContract,
		List<String> systemFields,
		List<AttachmentField> attachmentFields,
		Date publishedAt) {

	public record FieldContract(String key, String path, String type,
			String valueType, String itemType, boolean required,
			boolean multiple, List<JsonNode> enumValues) { }

	public record AttachmentField(String key, boolean multiple) { }
}
