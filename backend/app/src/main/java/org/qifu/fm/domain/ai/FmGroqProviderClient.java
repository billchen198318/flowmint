package org.qifu.fm.domain.ai;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class FmGroqProviderClient extends FmChatCompletionsProviderClient {

	public FmGroqProviderClient(ObjectMapper objectMapper,
			FmAiStructuredResultValidator resultValidator) {
		super(objectMapper, resultValidator, "GROQ", false);
	}
}
