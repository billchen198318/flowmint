package org.qifu.fm.domain.dataaction;

import java.util.Date;
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
		return parameters;
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

	@SuppressWarnings("unchecked")
	private Object readPath(Map<String, Object> request, String path) {
		if (path == null || !path.startsWith("$.")) {
			return null;
		}
		Object current = request;
		for (String segment : path.substring(2).split("\\.")) {
			if (!(current instanceof Map<?, ?> currentMap)) {
				return null;
			}
			current = ((Map<String, Object>) currentMap).get(segment);
		}
		return current;
	}
}
