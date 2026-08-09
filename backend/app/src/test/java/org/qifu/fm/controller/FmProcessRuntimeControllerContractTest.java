package org.qifu.fm.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.qifu.fm.dto.command.FmProcessSubmitRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FmProcessRuntimeControllerContractTest {

	@Test
	void exposesOnlyLoadAndSubmitRuntimeEndpoints() {
		RequestMapping root = FmProcessRuntimeController.class
				.getAnnotation(RequestMapping.class);
		assertNotNull(root);
		assertArrayEquals(new String[] { "/api/fm/requests" }, root.value());

		Set<String> paths = Arrays.stream(FmProcessRuntimeController.class
				.getDeclaredMethods())
				.map(method -> method.getAnnotation(PostMapping.class))
				.filter(annotation -> annotation != null)
				.flatMap(annotation -> Arrays.stream(annotation.value()))
				.collect(Collectors.toSet());
		assertEquals(Set.of("/start/load", "/submit"), paths);
		assertFalse(paths.stream().anyMatch(path -> path.contains("delete")));
	}

	@Test
	void submitBodyCannotOverrideTenantOrInitiator() {
		Set<String> components = Arrays.stream(FmProcessSubmitRequest.class
				.getRecordComponents())
				.map(component -> component.getName())
				.collect(Collectors.toSet());
		assertFalse(components.contains("tenantId"));
		assertFalse(components.contains("initiatorAccount"));
	}

	@Test
	void submitRequiresTenantAndIdempotencyHeaders() throws Exception {
		Method method = Arrays.stream(FmProcessRuntimeController.class
				.getDeclaredMethods())
				.filter(value -> value.getName().equals("submit"))
				.findFirst().orElseThrow();
		Set<String> headers = Arrays.stream(method.getParameterAnnotations())
				.flatMap(Arrays::stream)
				.filter(annotation -> annotation.annotationType().getName()
						.endsWith("RequestHeader"))
				.map(annotation -> (org.springframework.web.bind.annotation.RequestHeader)
						annotation)
				.map(org.springframework.web.bind.annotation.RequestHeader::value)
				.collect(Collectors.toSet());
		assertEquals(Set.of("X-FlowMint-Tenant", "Idempotency-Key"), headers);
	}
}
