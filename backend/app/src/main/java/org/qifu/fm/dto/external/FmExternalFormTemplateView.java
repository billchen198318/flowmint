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

	public record FieldContract(String key, String type, boolean required,
			boolean multiple) { }

	public record AttachmentField(String key, boolean multiple) { }
}
