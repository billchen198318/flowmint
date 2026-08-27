package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class FmOpenAiProviderClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FmOpenAiProviderClient client = new FmOpenAiProviderClient(
			objectMapper, new FmAiStructuredResultValidator());

	@Test
	void buildsResponsesApiRequestWithStructuredOutputAndNoStorage() throws Exception {
		HttpRequest request = client.buildRequest(config(), new FmAiAnalysisRequest(
				objectMapper.readTree("{\"form\":[]}"), "繁體中文", 1));

		assertEquals("https://api.openai.com/v1/responses", request.uri().toString());
		assertEquals("Bearer secret-key",
				request.headers().firstValue("Authorization").orElseThrow());
		assertTrue(request.bodyPublisher().isPresent());
	}

	@Test
	void parsesAndValidatesStructuredResponse() throws Exception {
		String result = """
				{
				  "summary": "摘要",
				  "keyFacts": ["事實"],
				  "risks": [{"level":"LOW","title":"風險","detail":"內容"}],
				  "questions": ["問題"],
				  "recommendation": {
				    "action": "REVIEW_REQUIRED",
				    "reason": "需人工確認"
				  },
				  "disclaimer": "僅供參考"
				}
				""";
		JsonNode response = objectMapper.createObjectNode()
				.put("id", "resp_1")
				.set("usage", objectMapper.createObjectNode()
						.put("input_tokens", 10)
						.put("output_tokens", 20));
		JsonNode content = objectMapper.createObjectNode()
				.put("type", "output_text")
				.put("text", result);
		((com.fasterxml.jackson.databind.node.ObjectNode) response)
				.putArray("output").addObject().putArray("content").add(content);

		FmAiAnalysisResponse parsed = client.parseResponse(response.toString());

		assertEquals("摘要", parsed.result().path("summary").asText());
		assertEquals(10, parsed.inputTokens());
		assertEquals(20, parsed.outputTokens());
		assertEquals("resp_1", parsed.providerResponseId());
	}

	@Test
	void rejectsUnknownRecommendationAction() throws Exception {
		JsonNode invalid = objectMapper.readTree("""
				{
				  "summary":"摘要",
				  "keyFacts":[],
				  "risks":[],
				  "questions":[],
				  "recommendation":{"action":"APPROVE","reason":"不允許"},
				  "disclaimer":"僅供參考"
				}
				""");

		assertThrows(ServiceException.class,
				() -> new FmAiStructuredResultValidator().validate(invalid));
		assertFalse(invalid.path("recommendation").path("action").asText().isBlank());
	}

	private FmAiProviderConfig config() {
		return new FmAiProviderConfig("OPENAI_MAIN", "OPENAI",
				"https://api.openai.com/v1/responses", "gpt-test", "secret-key",
				new BigDecimal("0.20"), 2000, 45);
	}
}
