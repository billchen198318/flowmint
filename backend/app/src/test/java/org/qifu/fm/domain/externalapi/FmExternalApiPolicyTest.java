package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;

class FmExternalApiPolicyTest {
	@Test
	void normalizesControlledClientValues() throws Exception {
		assertEquals("ERP", FmExternalApiPolicy.requireSystemType("erp"));
		assertEquals("ERP_PROD", FmExternalApiPolicy.normalizeClientCode(" erp_prod "));
		assertEquals(List.of("org.employee.read", "runtime.request.submit"),
				FmExternalApiPolicy.requireScopes(List.of("ORG.EMPLOYEE.READ",
						"runtime.request.submit", "org.employee.read")));
	}

	@Test
	void rejectsUnknownTypesCodesAndScopes() {
		assertThrows(ServiceException.class,
				() -> FmExternalApiPolicy.requireSystemType("CRM"));
		assertThrows(ServiceException.class,
				() -> FmExternalApiPolicy.normalizeClientCode("bad-code"));
		assertThrows(ServiceException.class,
				() -> FmExternalApiPolicy.requireScopes(List.of("admin.all")));
	}
}
