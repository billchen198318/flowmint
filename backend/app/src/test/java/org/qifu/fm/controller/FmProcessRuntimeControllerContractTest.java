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
import org.qifu.fm.dto.command.FmRequestCancelRequest;
import org.qifu.fm.dto.command.FmRequestWithdrawRequest;
import org.qifu.fm.dto.command.FmTaskActionRequest;
import org.qifu.fm.dto.command.FmTaskAddSignRequest;
import org.qifu.fm.dto.command.FmTaskTransferRequest;
import org.qifu.fm.dto.command.FmTaskDelegationRequest;
import org.qifu.fm.dto.command.FmTaskResolveRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class FmProcessRuntimeControllerContractTest {

	@Test
	void exposesOnlyRequiredRuntimeEndpoints() {
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
		assertEquals(Set.of(
				"/start/tenants",
				"/start/applicants",
				"/start/catalog",
				"/start/load",
				"/tasks/inbox",
				"/tasks/load",
				"/tasks/action",
				"/tasks/add-sign",
				"/tasks/add-sign-options",
				"/tasks/complete-add-sign",
				"/tasks/transfer",
				"/tasks/transfer-options",
				"/tasks/delegate",
				"/tasks/resolve",
				"/mine",
				"/mine/cancel",
				"/mine/diagram",
				"/mine/load",
				"/mine/withdraw",
				"/submit"), paths);
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

	@Test
	void taskActionSupportsValidatedFormResubmission() {
		Set<String> components = Arrays.stream(FmTaskActionRequest.class
				.getRecordComponents())
				.map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("taskId", "actionType", "comment", "reason",
				"targetTaskDefKey", "formData"), components);
	}

	@Test
	void withdrawBodyCannotOverrideTenantOrActor() {
		Set<String> components = Arrays.stream(FmRequestWithdrawRequest.class
				.getRecordComponents())
				.map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("processInstanceId", "reason"), components);
	}

	@Test
	void cancelBodyCannotOverrideTenantOrActor() {
		Set<String> components = Arrays.stream(FmRequestCancelRequest.class
				.getRecordComponents())
				.map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("processInstanceId", "reason"), components);
	}

	@Test
	void transferBodyCannotOverrideTenantOrActor() {
		Set<String> components = Arrays.stream(FmTaskTransferRequest.class
				.getRecordComponents())
				.map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("taskId", "targetAccount", "comment", "reason"),
				components);
	}

	@Test
	void delegationBodiesCannotOverrideTenantOrActor() {
		Set<String> delegateComponents = Arrays.stream(FmTaskDelegationRequest.class
				.getRecordComponents()).map(component -> component.getName())
				.collect(Collectors.toSet());
		Set<String> resolveComponents = Arrays.stream(FmTaskResolveRequest.class
				.getRecordComponents()).map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("taskId", "delegationId", "comment", "reason"),
				delegateComponents);
		assertEquals(Set.of("taskId", "comment"), resolveComponents);
	}

	@Test
	void addSignBodyCannotOverrideTenantOrActor() {
		Set<String> components = Arrays.stream(FmTaskAddSignRequest.class
				.getRecordComponents()).map(component -> component.getName())
				.collect(Collectors.toSet());
		assertEquals(Set.of("taskId", "targetAccount", "comment", "reason"),
				components);
	}
}
