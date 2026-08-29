package org.qifu.fm.domain.ai;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qifu.base.exception.ServiceException;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

@Component
public class FmAiStructuredResultValidator {

	private static final Set<String> ACTIONS = Set.of(
			"APPROVE_CONSIDERATION",
			"RETURN_CONSIDERATION",
			"REJECT_CONSIDERATION",
			"REVIEW_REQUIRED",
			"INSUFFICIENT_INFORMATION");

	public JsonNode validate(JsonNode result) throws ServiceException {
		if (result == null || !result.isObject()) {
			throw invalid();
		}
		requireText(result, "summary", 4000);
		requireArray(result, "keyFacts", 20);
		requireArray(result, "risks", 20);
		requireArray(result, "questions", 20);
		JsonNode recommendation = result.path("recommendation");
		if (!recommendation.isObject()
				|| !ACTIONS.contains(recommendation.path("action").asText())) {
			throw invalid();
		}
		requireText(recommendation, "reason", 2000);
		requireText(result, "disclaimer", 500);
		for (JsonNode risk : result.path("risks")) {
			if (!risk.isObject()
					|| !Set.of("LOW", "MEDIUM", "HIGH").contains(
							risk.path("level").asText())) {
				throw invalid();
			}
			requireText(risk, "title", 300);
			requireText(risk, "detail", 2000);
		}
		return result;
	}

	private void requireArray(JsonNode parent, String field, int maxItems)
			throws ServiceException {
		JsonNode value = parent.path(field);
		if (!value.isArray() || value.size() > maxItems) {
			throw invalid();
		}
		for (JsonNode item : value) {
			if ("risks".equals(field)) {
				continue;
			}
			if (!item.isTextual() || item.asText().length() > 2000) {
				throw invalid();
			}
		}
	}

	private void requireText(JsonNode parent, String field, int maxLength)
			throws ServiceException {
		JsonNode value = parent.path(field);
		if (!value.isTextual() || StringUtils.isBlank(value.asText())
				|| value.asText().length() > maxLength) {
			throw invalid();
		}
	}

	private ServiceException invalid() {
		return new ServiceException("AI Provider 回應格式不符合安全契約");
	}
}
