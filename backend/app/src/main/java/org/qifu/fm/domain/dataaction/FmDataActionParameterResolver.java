package org.qifu.fm.domain.dataaction;

import java.util.Date;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class FmDataActionParameterResolver {

	private final ObjectMapper objectMapper;

	public FmDataActionParameterResolver(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Map<String, Object> resolve(String requestSchema,
			Map<String, Object> request, String tenantId, String loginAccount)
			throws ServiceException {
		Map<String, Object> parameters = new LinkedHashMap<>();
		if (requestSchema != null && !requestSchema.isBlank()) {
			Map<String, String> mappings = readMappings(requestSchema);
			mappings.forEach((name, path) ->
					parameters.put(name, readPath(request, path)));
		} else {
			parameters.putAll(request);
		}
		parameters.put("tenantId", tenantId);
		parameters.put("loginAccount", loginAccount);
		parameters.put("now", new Date());
		copyPagination(request, parameters, "page");
		copyPagination(request, parameters, "pageSize");
		return parameters;
	}

	private void copyPagination(Map<String, Object> request,
			Map<String, Object> parameters, String name) throws ServiceException {
		Object value = request.get(name);
		if (value == null) {
			return;
		}
		try {
			int number = value instanceof Number numeric
					? numeric.intValue() : Integer.parseInt(String.valueOf(value));
			if (number < 1 || ("page".equals(name) && number > 1_000_000)) {
				throw new NumberFormatException();
			}
			parameters.put(name, number);
		} catch (NumberFormatException exception) {
			throw new ServiceException("分頁參數不正確：" + name);
		}
	}

	public Map<String, Object> resolveForStep(String requestSchema,
			Map<String, Object> baseParameters, Map<String, Object> request,
			Map<String, Object> stepResults, Object item) throws ServiceException {
		Map<String, Object> parameters = new LinkedHashMap<>(baseParameters);
		if (requestSchema == null || requestSchema.isBlank()) {
			if (item instanceof Map<?, ?> itemMap) {
				itemMap.forEach((key, value) ->
						parameters.put(String.valueOf(key), value));
			}
			return parameters;
		}
		Map<String, Object> context = new LinkedHashMap<>();
		context.put("request", request);
		context.put("steps", stepResults);
		context.put("item", item);
		for (Map.Entry<String, String> mapping : readMappings(requestSchema).entrySet()) {
			String expression = mapping.getValue();
			if (expression != null && expression.startsWith("${")
					&& expression.endsWith("}")) {
				parameters.put(mapping.getKey(), readPath(context,
						"$." + expression.substring(2, expression.length() - 1)));
			}
		}
		return parameters;
	}

	public Object readRequestPath(Map<String, Object> request, String path) {
		return readPath(request, path);
	}

	private Map<String, String> readMappings(String requestSchema)
			throws ServiceException {
		try {
			return objectMapper.readValue(requestSchema,
					new TypeReference<Map<String, String>>() { });
		} catch (Exception exception) {
			throw new ServiceException(BaseSystemMessage.parameterIncorrect());
		}
	}

	private Object readPath(Map<String, Object> request, String path) {
		if (path == null || !path.startsWith("$.")) {
			return null;
		}
		Object current = request;
		for (String segment : path.substring(2).split("\\.")) {
			int bracket = segment.indexOf('[');
			String property = bracket < 0 ? segment : segment.substring(0, bracket);
			if (!property.isEmpty()) {
				if (!(current instanceof Map<?, ?> currentMap)) {
					return null;
				}
				current = currentMap.get(property);
			}
			while (bracket >= 0) {
				int close = segment.indexOf(']', bracket);
				if (close < 0 || !(current instanceof List<?> list)) {
					return null;
				}
				try {
					int index = Integer.parseInt(segment.substring(bracket + 1, close));
					current = index >= 0 && index < list.size() ? list.get(index) : null;
				} catch (NumberFormatException exception) {
					return null;
				}
				bracket = segment.indexOf('[', close + 1);
			}
		}
		return current;
	}
}
