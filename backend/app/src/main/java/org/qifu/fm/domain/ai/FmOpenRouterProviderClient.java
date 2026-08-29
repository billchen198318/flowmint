package org.qifu.fm.domain.ai;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class FmOpenRouterProviderClient extends FmChatCompletionsProviderClient {

	public FmOpenRouterProviderClient(ObjectMapper objectMapper,
			FmAiStructuredResultValidator resultValidator) {
		super(objectMapper, resultValidator, "OPENROUTER", true);
	}
}
