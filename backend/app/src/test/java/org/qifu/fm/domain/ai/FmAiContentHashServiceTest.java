package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmAiContentHashServiceTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FmAiContentHashService service =
			new FmAiContentHashService(objectMapper);

	@Test
	void canonicalizesObjectPropertyOrder() throws Exception {
		String first = service.hash("T1", "P1", "TASK1", 3,
				objectMapper.readTree("{\"b\":2,\"a\":1}"), 4, 1);
		String second = service.hash("T1", "P1", "TASK1", 3,
				objectMapper.readTree("{\"a\":1,\"b\":2}"), 4, 1);

		assertEquals(first, second);
		assertEquals(64, first.length());
	}

	@Test
	void changesWhenRevisionHistoryOrProviderConfigurationChanges() throws Exception {
		String baseline = service.hash("T1", "P1", "TASK1", 3,
				objectMapper.readTree("{\"history\":[]}"), 4, 1);

		assertNotEquals(baseline, service.hash("T1", "P1", "TASK1", 4,
				objectMapper.readTree("{\"history\":[]}"), 4, 1));
		assertNotEquals(baseline, service.hash("T1", "P1", "TASK1", 3,
				objectMapper.readTree("{\"history\":[{\"action\":\"APPROVE\"}]}"),
				4, 1));
		assertNotEquals(baseline, service.hash("T1", "P1", "TASK1", 3,
				objectMapper.readTree("{\"history\":[]}"), 5, 1));
	}
}
