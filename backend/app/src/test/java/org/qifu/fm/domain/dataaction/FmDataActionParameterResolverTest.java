package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class FmDataActionParameterResolverTest {

	private final FmDataActionParameterResolver resolver =
			new FmDataActionParameterResolver(new ObjectMapper());

	@Test
	void resolvesNestedJsonPathAndProtectsServerContext() throws Exception {
		Map<String, Object> request = Map.of(
				"tenantId", "FORGED",
				"formData", Map.of("amount", 1200),
				"employeeIds", List.of("E1", "E2"));
		String mapping = """
				{
				  "amount": "$.formData.amount",
				  "employeeIds": "$.employeeIds",
				  "tenantId": "$.tenantId"
				}
				""";

		Map<String, Object> parameters = resolver.resolve(
				mapping, request, "TENANT-A", "user01");

		assertEquals(1200, parameters.get("amount"));
		assertEquals(List.of("E1", "E2"), parameters.get("employeeIds"));
		assertEquals("TENANT-A", parameters.get("tenantId"));
		assertEquals("user01", parameters.get("loginAccount"));
	}

	@Test
	void resolvesItemAndPreviousStepExpressions() throws Exception {
		String mapping = """
				{
				  "headerId": "${steps.header.generatedKey}",
				  "itemCode": "${item.itemCode}",
				  "status": "${steps.validation.rows[0].status}"
				}
				""";
		Map<String, Object> base = resolver.resolve(mapping, Map.of(),
				"TENANT-A", "user01");
		Map<String, Object> steps = Map.of(
				"header", Map.of("generatedKey", 99L),
				"validation", Map.of("rows", List.of(Map.of("status", "OK"))));

		Map<String, Object> parameters = resolver.resolveForStep(mapping, base,
				Map.of(), steps, Map.of("itemCode", "A01"));

		assertEquals(99L, parameters.get("headerId"));
		assertEquals("A01", parameters.get("itemCode"));
		assertEquals("OK", parameters.get("status"));
	}

	@Test
	void stepMappingCannotOverrideServerContext() throws Exception {
		String mapping = """
				{"tenantId":"${item.tenantId}",
				 "loginAccount":"${steps.previous.loginAccount}",
				 "now":"${item.now}",
				 "itemCode":"${item.itemCode}"}
				""";
		Map<String, Object> base = resolver.resolve(mapping, Map.of(),
				"TENANT-A", "user01");
		Map<String, Object> resolved = resolver.resolveForStep(mapping, base,
				Map.of(), Map.of("previous", Map.of("loginAccount", "forged")),
				Map.of("tenantId", "TENANT-B", "now", "forged",
						"itemCode", "A01"));
		assertEquals("TENANT-A", resolved.get("tenantId"));
		assertEquals("user01", resolved.get("loginAccount"));
		assertEquals(base.get("now"), resolved.get("now"));
		assertEquals("A01", resolved.get("itemCode"));
	}

	@Test
	void rawItemCannotOverrideServerContextWithoutSchema() throws Exception {
		Map<String, Object> base = resolver.resolve(null, Map.of(),
				"TENANT-A", "user01");
		Map<String, Object> resolved = resolver.resolveForStep(null, base,
				Map.of(), Map.of(), Map.of("tenantId", "TENANT-B",
						"loginAccount", "forged", "sku", "A01"));
		assertEquals("TENANT-A", resolved.get("tenantId"));
		assertEquals("user01", resolved.get("loginAccount"));
		assertEquals("A01", resolved.get("sku"));
	}
}
