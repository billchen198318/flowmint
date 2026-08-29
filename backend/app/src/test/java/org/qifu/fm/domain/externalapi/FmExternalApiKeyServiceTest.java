package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class FmExternalApiKeyServiceTest {

	private static final String TEST_PEPPER =
			"MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

	@Test
	void generatesOpaqueKeyAndVerifiesOnlyMatchingSecret() throws Exception {
		FmExternalApiKeyService service = service();
		var generated = service.generate();

		assertTrue(generated.plainText().startsWith("fmk_live_"));
		assertEquals(generated.keyId(), service.extractKeyId(generated.plainText()));
		assertTrue(service.matches(generated.plainText(), generated.secretHash()));
		assertFalse(service.matches(generated.plainText() + "x", generated.secretHash()));
		assertFalse(service.matches(generated.plainText(), "invalid"));
	}

	@Test
	void createsIndependentKeysAndRejectsMalformedIdentifiers() throws Exception {
		FmExternalApiKeyService service = service();
		var first = service.generate();
		var second = service.generate();

		assertNotEquals(first.plainText(), second.plainText());
		assertNotEquals(first.secretHash(), second.secretHash());
		assertNull(service.extractKeyId("not-a-flowmint-key"));
	}

	private FmExternalApiKeyService service() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("fm.external-api.key-pepper", TEST_PEPPER);
		return new FmExternalApiKeyService(environment);
	}
}
