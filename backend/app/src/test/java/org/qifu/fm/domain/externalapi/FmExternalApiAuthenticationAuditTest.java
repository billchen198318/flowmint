package org.qifu.fm.domain.externalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.fm.entity.FmApiAccessLog;
import org.qifu.fm.filter.FmExternalApiAuthenticationFilter;
import org.qifu.fm.service.IFmApiAccessLogService;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import tools.jackson.databind.ObjectMapper;

class FmExternalApiAuthenticationAuditTest {

	@Test
	void anonymousFailureRecordsSafeFields() {
		IFmApiAccessLogService service = mock(IFmApiAccessLogService.class);
		FmExternalApiAccessLedger ledger = new FmExternalApiAccessLedger(service);
		ledger.recordAuthenticationFailure("192.0.2.1", "request-1", null,
				"forms", "form:read", "test", 2, -1, -1,
				"private-idempotency-key", new Date());

		ArgumentCaptor<FmApiAccessLog> captor = ArgumentCaptor.forClass(FmApiAccessLog.class);
		verify(service).append(captor.capture());
		FmApiAccessLog access = captor.getValue();
		assertNull(access.getTenantId());
		assertNull(access.getClientId());
		assertNull(access.getKeyId());
		assertEquals("192.0.2.1", access.getSourceIp());
		assertEquals(401, access.getHttpStatus());
		assertEquals("AUTHENTICATION_FAILED", access.getResultCode());
		assertEquals("UNAUTHENTICATED", access.getCuserid());
		assertNotEquals("private-idempotency-key", access.getIdempotencyKeyHash());
		assertEquals(64, access.getIdempotencyKeyHash().length());
	}

	@Test
	void rejectedKeyIsAuditedBeforeFilterReturns() throws Exception {
		FmExternalApiAuthenticator authenticator = mock(FmExternalApiAuthenticator.class);
		when(authenticator.authenticate(eq("invalid-key"), eq("192.0.2.1")))
				.thenThrow(new IllegalArgumentException("invalid key"));
		FmExternalApiAccessLedger ledger = mock(FmExternalApiAccessLedger.class);
		FmExternalApiAuthenticationFilter filter = new FmExternalApiAuthenticationFilter(
				authenticator, mock(IFmApiClientKeyService.class),
				mock(FmExternalApiQuotaGuard.class), ledger, new ObjectMapper());
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
				"/api/fm/external/v1/forms");
		request.setServletPath("/api/fm/external/v1/forms");
		request.setRemoteAddr("192.0.2.1");
		request.addHeader("Authorization", "Bearer invalid-key");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertNull(chain.getRequest());
		verify(ledger).recordAuthenticationFailure(eq("192.0.2.1"), any(),
				any(), eq("forms"), any(), any(), any(Long.class),
				any(Long.class), any(Long.class), any(), any(Date.class));
	}
}
