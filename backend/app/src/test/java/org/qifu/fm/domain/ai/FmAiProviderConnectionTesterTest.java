package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.Test;

class FmAiProviderConnectionTesterTest {

	private final FmAiProviderConnectionTester tester =
			new FmAiProviderConnectionTester();

	@Test
	void bearerProvidersUseAuthorizationHeader() throws Exception {
		HttpRequest request = tester.request(config("OPENAI",
				"https://api.openai.com/v1"));

		assertEquals("https://api.openai.com/v1/models",
				request.uri().toString());
		assertEquals("Bearer secret-value",
				request.headers().firstValue("Authorization").orElseThrow());
		assertFalse(request.uri().toString().contains("secret-value"));
	}

	@Test
	void geminiUsesApiKeyHeaderAndNeverPlacesKeyInUrl() throws Exception {
		HttpRequest request = tester.request(config("GEMINI",
				"https://generativelanguage.googleapis.com"));

		assertEquals("https://generativelanguage.googleapis.com/v1beta/models",
				request.uri().toString());
		assertEquals("secret-value",
				request.headers().firstValue("x-goog-api-key").orElseThrow());
		assertFalse(request.uri().toString().contains("secret-value"));
	}

	private FmAiProviderConfig config(String type, String baseUrl) {
		return new FmAiProviderConfig("TEST", type, baseUrl, "model",
				"secret-value", new BigDecimal("0.20"), 2000, 45);
	}
}
