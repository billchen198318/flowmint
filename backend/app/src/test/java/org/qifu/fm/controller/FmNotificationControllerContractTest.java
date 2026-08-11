package org.qifu.fm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.qifu.fm.dto.command.FmNotificationReadRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FmNotificationControllerContractTest {

	@Test
	void exposesOnlyCurrentUsersTenantScopedInboxOperations() {
		assertEquals("/api/fm/notifications", FmNotificationController.class
				.getAnnotation(RequestMapping.class).value()[0]);
		Set<String> paths = Arrays.stream(FmNotificationController.class
				.getDeclaredMethods()).map(method -> method.getAnnotation(PostMapping.class))
				.filter(annotation -> annotation != null)
				.flatMap(annotation -> Arrays.stream(annotation.value()))
				.collect(Collectors.toSet());
		assertEquals(Set.of("/inbox", "/read", "/read-all"), paths);
	}

	@Test
	void readBodyCannotOverrideTenantOrRecipient() {
		Set<String> components = Arrays.stream(FmNotificationReadRequest.class
				.getRecordComponents()).map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("notificationId"), components);
		assertFalse(components.contains("tenantId"));
		assertFalse(components.contains("recipientAccount"));
	}
}
