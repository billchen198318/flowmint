package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmAiProviderCatalogTest {

	@Test
	void acceptsOfficialHttpsEndpoint() throws Exception {
		assertEquals("https://api.openai.com/v1/responses",
				FmAiProviderCatalog.requireBaseUrl(
						"OPENAI", "https://api.openai.com/v1/responses"));
	}

	@Test
	void rejectsUnexpectedHostAndUnsafeUrlParts() {
		assertThrows(ServiceException.class, () ->
				FmAiProviderCatalog.requireBaseUrl("OPENAI", "https://example.com/v1"));
		assertThrows(ServiceException.class, () ->
				FmAiProviderCatalog.requireBaseUrl(
						"OPENAI", "https://api.openai.com/v1?key=secret"));
		assertThrows(ServiceException.class, () ->
				FmAiProviderCatalog.requireBaseUrl("OPENAI", "http://api.openai.com/v1"));
	}
}
