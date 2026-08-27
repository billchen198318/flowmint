package org.qifu.fm.domain.ai;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.dto.view.FmTaskActionView;
import org.qifu.fm.dto.view.FmTaskDetailView;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class FmAiContextBuilder {

	private static final Set<String> CONTAINER_TYPES = Set.of(
			"container", "columns", "fieldset", "panel", "table", "tabs",
			"well", "datagrid", "editgrid");
	private static final Set<String> EXCLUDED_TYPES = Set.of(
			"password", "hidden", "button", "htmlelement", "content");
	private static final Set<String> SENSITIVE_KEY_PARTS = Set.of(
			"password", "passwd", "secret", "token", "apikey", "api_key",
			"authorization", "cookie", "sql", "script", "credential");

	private final ObjectMapper objectMapper;

	public FmAiContextBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ObjectNode build(FmTaskDetailView detail) throws ServiceException {
		if (detail == null || detail.task() == null) {
			throw new ServiceException("AI Context 缺少 Task 資料");
		}
		ObjectNode context = objectMapper.createObjectNode();
		ObjectNode process = context.putObject("process");
		process.put("name", safe(detail.task().processName(), 300));
		process.put("documentNumber", safe(detail.task().documentNumber(), 200));
		process.put("currentTask", safe(detail.task().taskName(), 300));
		ObjectNode applicant = context.putObject("applicant");
		applicant.put("account", safe(detail.task().applicantAccount(), 100));
		ArrayNode form = context.putArray("form");
		JsonNode schema = parseSchema(detail.schemaContent());
		collectComponents(schema.path("components"), detail.formData(), form, "");
		ArrayNode history = context.putArray("history");
		if (detail.actions() != null) {
			for (FmTaskActionView action : detail.actions().stream().limit(50).toList()) {
				ObjectNode item = history.addObject();
				item.put("action", safe(action.actionType(), 100));
				item.put("actor", safe(action.actorAccount(), 100));
				item.put("comment", safe(action.comment(), 2000));
				item.put("reason", safe(action.reason(), 2000));
				if (action.actionDate() != null) {
					item.put("actionDate", action.actionDate().toInstant().toString());
				}
			}
		}
		return context;
	}

	private void collectComponents(JsonNode components, Map<String, Object> data,
			ArrayNode output, String prefix) {
		if (!components.isArray()) {
			return;
		}
		for (JsonNode component : components) {
			String type = StringUtils.lowerCase(component.path("type").asText());
			String key = component.path("key").asText();
			if (excluded(component, type, key)) {
				continue;
			}
			JsonNode nested = component.path("components");
			if (nested.isArray() || CONTAINER_TYPES.contains(type)) {
				collectNested(component, data, output, prefix, key);
				continue;
			}
			if (StringUtils.isBlank(key) || data == null || !data.containsKey(key)) {
				continue;
			}
			ObjectNode field = output.addObject();
			field.put("label", safe(component.path("label").asText(key), 300));
			field.put("key", prefix + key);
			field.set("value", safeValue(objectMapper.valueToTree(data.get(key))));
		}
	}

	private void collectNested(JsonNode component, Map<String, Object> data,
			ArrayNode output, String prefix, String key) {
		JsonNode nested = component.path("components");
		if (nested.isArray()) {
			collectComponents(nested, data, output, prefix);
		}
		for (JsonNode column : component.path("columns")) {
			collectComponents(column.path("components"), data, output, prefix);
		}
		for (JsonNode row : component.path("rows")) {
			for (JsonNode cell : row) {
				collectComponents(cell.path("components"), data, output, prefix);
			}
		}
		if (Set.of("datagrid", "editgrid").contains(
				StringUtils.lowerCase(component.path("type").asText()))
				&& StringUtils.isNotBlank(key) && data != null && data.containsKey(key)) {
			ObjectNode field = output.addObject();
			field.put("label", safe(component.path("label").asText(key), 300));
			field.put("key", prefix + key);
			field.set("value", safeValue(objectMapper.valueToTree(data.get(key))));
		}
	}

	private boolean excluded(JsonNode component, String type, String key) {
		if (component.path("hidden").asBoolean(false) || EXCLUDED_TYPES.contains(type)) {
			return true;
		}
		String normalized = StringUtils.defaultString(key).toLowerCase(Locale.ROOT);
		return SENSITIVE_KEY_PARTS.stream().anyMatch(normalized::contains);
	}

	private JsonNode safeValue(JsonNode value) {
		if (value == null || value.isNull()) {
			return objectMapper.nullNode();
		}
		if (value.isTextual()) {
			return objectMapper.getNodeFactory().textNode(safe(value.asText(), 4000));
		}
		if (value.isArray()) {
			ArrayNode result = objectMapper.createArrayNode();
			int count = 0;
			for (JsonNode item : value) {
				if (count++ >= 100) {
					break;
				}
				result.add(safeValue(item));
			}
			return result;
		}
		if (value.isObject()) {
			ObjectNode result = objectMapper.createObjectNode();
			Iterator<Map.Entry<String, JsonNode>> fields = value.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (!SENSITIVE_KEY_PARTS.stream().anyMatch(
						field.getKey().toLowerCase(Locale.ROOT)::contains)) {
					result.set(field.getKey(), safeValue(field.getValue()));
				}
			}
			return result;
		}
		return value;
	}

	private JsonNode parseSchema(String schemaContent) throws ServiceException {
		try {
			JsonNode schema = objectMapper.readTree(schemaContent);
			if (schema == null || !schema.isObject()) {
				throw new ServiceException("AI Context 表單 Schema 無效");
			}
			return schema;
		} catch (JsonProcessingException exception) {
			throw new ServiceException("AI Context 表單 Schema 無效");
		}
	}

	private String safe(String value, int maxLength) {
		return StringUtils.abbreviate(StringUtils.defaultString(value), maxLength);
	}
}
