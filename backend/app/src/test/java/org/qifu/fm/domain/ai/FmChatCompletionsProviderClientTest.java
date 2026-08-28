package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmChatCompletionsProviderClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FmAiStructuredResultValidator validator =
			new FmAiStructuredResultValidator();

	@Test
	void buildsOfficialGroqChatCompletionsEndpoint() throws Exception {
		FmGroqProviderClient client = new FmGroqProviderClient(objectMapper, validator);
		HttpRequest request = client.buildRequest(config("GROQ",
				"https://api.groq.com/openai/v1", "llama-test"), request());

		assertEquals("https://api.groq.com/openai/v1/chat/completions",
				request.uri().toString());
		assertEquals("Bearer secret-key",
				request.headers().firstValue("Authorization").orElseThrow());
	}

	@Test
	void buildsOfficialOpenRouterChatCompletionsEndpoint() throws Exception {
		FmOpenRouterProviderClient client = new FmOpenRouterProviderClient(
				objectMapper, validator);
		HttpRequest request = client.buildRequest(config("OPENROUTER",
				"https://openrouter.ai/api/v1", "openai/test"), request());

		assertEquals("https://openrouter.ai/api/v1/chat/completions",
				request.uri().toString());
	}

	@Test
	void parsesChatCompletionAndValidatesLocalSafetyContract() throws Exception {
		FmGroqProviderClient client = new FmGroqProviderClient(objectMapper, validator);
		String result = """
				{\"summary\":\"摘要\",\"keyFacts\":[\"事實\"],
				 \"risks\":[],\"questions\":[],
				 \"recommendation\":{\"action\":\"REVIEW_REQUIRED\",\"reason\":\"人工確認\"},
				 \"disclaimer\":\"僅供參考\"}
				""".replace("\n", "");
		String response = objectMapper.createObjectNode().put("id", "chat_1")
				.set("usage", objectMapper.createObjectNode()
						.put("prompt_tokens", 11).put("completion_tokens", 22))
				.toString();
		var root = (tools.jackson.databind.node.ObjectNode) objectMapper.readTree(response);
		root.putArray("choices").addObject().putObject("message").put("content", result);

		FmAiAnalysisResponse parsed = client.parseResponse(root.toString());

		assertEquals("摘要", parsed.result().path("summary").asText());
		assertEquals(11, parsed.inputTokens());
		assertEquals(22, parsed.outputTokens());
	}

	private FmAiAnalysisRequest request() throws Exception {
		return new FmAiAnalysisRequest(objectMapper.readTree("{\"form\":[]}"),
				"繁體中文", 1);
	}

	private FmAiProviderConfig config(String type, String baseUrl, String model) {
		return new FmAiProviderConfig("TEST", type, baseUrl, model, "secret-key",
				new BigDecimal("0.20"), 2000, 45);
	}
}
