package org.qifu.fm.domain.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmAiStructuredResultValidatorPrivacyTest {
	@Test
	void ignoresUnknownFieldsAtEveryLevel() throws Exception {
		var input = new ObjectMapper().readTree("""
				{"summary":"Summary","keyFacts":[],"risks":[
				 {"level":"LOW","title":"Title","detail":"Detail","extra":"secret"}],
				 "questions":[],"recommendation":{"action":"REVIEW_REQUIRED",
				 "reason":"Review","extra":"secret"},"disclaimer":"Disclaimer",
				 "extra":"secret"}
				""");
		var result = new FmAiStructuredResultValidator().validate(input);
		assertFalse(result.toString().contains("secret"));
	}
}
