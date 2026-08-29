package org.qifu.fm.domain.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class FmAiContentHashService {

	private final ObjectMapper objectMapper;

	public FmAiContentHashService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String hash(String tenantId, String processInstanceId, String taskId,
			Integer formRevision, JsonNode context, Integer providerConfigVersion,
			Integer promptTemplateVersion) throws ServiceException {
		ObjectNode content = objectMapper.createObjectNode();
		content.put("tenantId", tenantId);
		content.put("processInstanceId", processInstanceId);
		content.put("taskId", taskId);
		content.put("formRevision", formRevision);
		content.set("context", canonical(context));
		content.put("providerConfigVersion", providerConfigVersion);
		content.put("promptTemplateVersion", promptTemplateVersion);
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(content);
			return java.util.HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (JacksonException | NoSuchAlgorithmException exception) {
			throw new ServiceException("AI Content Hash 建立失敗");
		}
	}

	private JsonNode canonical(JsonNode value) {
		if (value == null || value.isNull()) {
			return objectMapper.nullNode();
		}
		if (value.isArray()) {
			ArrayNode result = objectMapper.createArrayNode();
			value.forEach(item -> result.add(canonical(item)));
			return result;
		}
		if (value.isObject()) {
			ObjectNode result = objectMapper.createObjectNode();
			List<String> names = new ArrayList<>();
			value.properties().forEach(property -> names.add(property.getKey()));
			names.sort(Comparator.naturalOrder());
			names.forEach(name -> result.set(name, canonical(value.get(name))));
			return result;
		}
		return value;
	}
}
