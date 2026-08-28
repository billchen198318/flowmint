package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmGeminiProviderClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FmGeminiProviderClient client = new FmGeminiProviderClient(
			objectMapper, new FmAiStructuredResultValidator());

	@Test
	void buildsOfficialGenerateContentEndpointAndApiKeyHeader() throws Exception {
		HttpRequest built = client.buildRequest(config(), request());

		assertEquals("https://generativelanguage.googleapis.com/v1beta/models/"
				+ "gemini-2.5-flash:generateContent", built.uri().toString());
		assertEquals("secret-key",
				built.headers().firstValue("x-goog-api-key").orElseThrow());
	}

	@Test
	void parsesCandidateAndUsageAfterLocalContractValidation() throws Exception {
		String result = """
				{"summary":"摘要","keyFacts":["事實"],"risks":[],"questions":[],
				 "recommendation":{"action":"REVIEW_REQUIRED","reason":"需要覆核"},
				 "disclaimer":"僅供參考"}
				""".replace("\n", "");
		var root = objectMapper.createObjectNode().put("responseId", "gemini_1");
		root.putArray("candidates").addObject().putObject("content")
				.putArray("parts").addObject().put("text", result);
		root.putObject("usageMetadata").put("promptTokenCount", 10)
				.put("candidatesTokenCount", 20);

		FmAiAnalysisResponse parsed = client.parseResponse(root.toString());

		assertEquals("摘要", parsed.result().path("summary").asText());
		assertEquals(10, parsed.inputTokens());
		assertEquals(20, parsed.outputTokens());
		assertEquals("gemini_1", parsed.providerResponseId());
	}

	private FmAiAnalysisRequest request() throws Exception {
		return new FmAiAnalysisRequest(objectMapper.readTree("{\"form\":[]}"),
				"繁體中文", 1);
	}

	private FmAiProviderConfig config() {
		return new FmAiProviderConfig("TEST", "GEMINI",
				"https://generativelanguage.googleapis.com", "gemini-2.5-flash",
				"secret-key", new BigDecimal("0.20"), 2000, 45);
	}
}
