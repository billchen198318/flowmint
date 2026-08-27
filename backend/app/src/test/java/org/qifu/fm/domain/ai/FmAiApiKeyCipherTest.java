package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.springframework.mock.env.MockEnvironment;

class FmAiApiKeyCipherTest {

	private static final String TEST_KEY =
			"MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

	@Test
	void encryptsDecryptsAndMasksApiKey() throws Exception {
		FmAiApiKeyCipher cipher = cipher();
		String encrypted = cipher.encrypt("sk-example-1234");

		assertNotEquals("sk-example-1234", encrypted);
		assertEquals("sk-example-1234", cipher.decrypt(encrypted));
		assertEquals("****1234", cipher.mask(encrypted));
	}

	@Test
	void rejectsBlankApiKey() {
		assertThrows(ServiceException.class, () -> cipher().encrypt(" "));
	}

	private FmAiApiKeyCipher cipher() {
		MockEnvironment environment = new MockEnvironment();
		environment.setProperty("fm.ai.encryption-key", TEST_KEY);
		return new FmAiApiKeyCipher(environment);
	}
}
