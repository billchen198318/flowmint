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
}
