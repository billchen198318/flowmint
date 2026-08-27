package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class FmDataActionContinueConditionEvaluatorTest {
	private final FmDataActionContinueConditionEvaluator evaluator =
			new FmDataActionContinueConditionEvaluator(
					new FmDataActionParameterResolver(new ObjectMapper()));

	@Test
	void supportsNumericDateNullAndBooleanOperators() throws Exception {
		Map<String, Object> request = Map.of("amount", 1200,
				"date", "2026-08-25", "enabled", true);
		assertTrue(evaluator.evaluate(
				"${request.amount} >= 1000 && ${request.enabled} == true",
				request, Map.of()));
		assertTrue(evaluator.evaluate(
				"${request.date} < '2026-09-01' || ${request.missing} != null",
				request, Map.of()));
		assertTrue(evaluator.evaluate("${request.missing} == null", request, Map.of()));
	}

	@Test
	void andHasPrecedenceOverOr() throws Exception {
		Map<String, Object> request = Map.of("a", false, "b", true, "c", false);
		assertFalse(evaluator.evaluate(
				"${request.a} == true || ${request.b} == true && ${request.c} == true",
				request, Map.of()));
	}
}
