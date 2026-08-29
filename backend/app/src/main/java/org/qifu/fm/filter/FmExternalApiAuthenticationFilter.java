package org.qifu.fm.filter;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.fm.domain.externalapi.FmExternalApiAuthenticator;
import org.qifu.fm.domain.externalapi.FmExternalApiAccessLedger;
import org.qifu.fm.domain.externalapi.FmExternalApiContext;
import org.qifu.fm.domain.externalapi.FmExternalApiEndpointCatalog;
import org.qifu.fm.domain.externalapi.FmExternalApiPrincipal;
import org.qifu.fm.domain.externalapi.FmExternalApiQuotaGuard;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FmExternalApiAuthenticationFilter extends OncePerRequestFilter {

	public static final String BASE_PATH = "/api/fm/external/v1/";
	public static final String REQUEST_ID_ATTRIBUTE =
			FmExternalApiAuthenticationFilter.class.getName() + ".requestId";
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final String MDC_REQUEST_ID = "externalRequestId";
	private static final Logger LOGGER = LoggerFactory.getLogger(
			FmExternalApiAuthenticationFilter.class);
	private final FmExternalApiAuthenticator authenticator;
	private final IFmApiClientKeyService keyService;
	private final FmExternalApiQuotaGuard quotaGuard;
	private final FmExternalApiAccessLedger accessLedger;
	private final ObjectMapper objectMapper;

	public FmExternalApiAuthenticationFilter(FmExternalApiAuthenticator authenticator,
			IFmApiClientKeyService keyService, FmExternalApiQuotaGuard quotaGuard,
			FmExternalApiAccessLedger accessLedger, ObjectMapper objectMapper) {
		this.authenticator = authenticator;
		this.keyService = keyService;
		this.quotaGuard = quotaGuard;
		this.accessLedger = accessLedger;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getServletPath().startsWith(BASE_PATH);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = requestId(request.getHeader(REQUEST_ID_HEADER));
		Date requestDate = new Date();
		long startedAt = System.nanoTime();
		response.setHeader(REQUEST_ID_HEADER, requestId);
		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		MDC.put(MDC_REQUEST_ID, requestId);
		FmExternalApiPrincipal principal;
		try {
			String apiKey = bearerToken(request.getHeader("Authorization"));
			principal = authenticator.authenticate(
					apiKey, request.getRemoteAddr());
			FmExternalApiContext.set(principal);
			keyService.markUsed(principal.keyId(), new Date(), principal.sourceIp());
		} catch (Exception exception) {
			error(response, requestId, HttpServletResponse.SC_UNAUTHORIZED,
					"AUTHENTICATION_FAILED", "External API authentication failed.");
			FmExternalApiContext.clear();
			MDC.remove(MDC_REQUEST_ID);
			return;
		}
		try {
			String requiredScope = FmExternalApiEndpointCatalog.requiredScope(
					endpointCode(request));
			if (requiredScope == null || !principal.hasScope(requiredScope)) {
				error(response, requestId, HttpServletResponse.SC_FORBIDDEN,
						"SCOPE_DENIED", "External API scope is not granted.");
				record(request, response, principal, requestId, requestDate, startedAt,
						"SCOPE_DENIED");
				FmExternalApiContext.clear();
				MDC.remove(MDC_REQUEST_ID);
				return;
			}
			quotaGuard.requireAvailable(principal);
		} catch (Exception exception) {
			error(response, requestId, 429, "QUOTA_EXCEEDED",
					"External API request quota exceeded.");
			record(request, response, principal, requestId, requestDate, startedAt,
					"QUOTA_EXCEEDED");
			FmExternalApiContext.clear();
			MDC.remove(MDC_REQUEST_ID);
			return;
		}
		try {
			filterChain.doFilter(request, response);
		} finally {
			record(request, response, principal, requestId, requestDate, startedAt,
					response.getStatus() < 400 ? "SUCCESS" : "HTTP_ERROR");
			FmExternalApiContext.clear();
			MDC.remove(MDC_REQUEST_ID);
		}
	}

	private String bearerToken(String authorization) {
		if (StringUtils.isBlank(authorization)
				|| !authorization.regionMatches(true, 0, BEARER_PREFIX, 0,
						BEARER_PREFIX.length())) {
			return null;
		}
		return StringUtils.trimToNull(authorization.substring(BEARER_PREFIX.length()));
	}

	private String requestId(String supplied) {
		String value = StringUtils.trimToEmpty(supplied);
		return value.matches("[A-Za-z0-9._-]{1,64}")
				? value : UUID.randomUUID().toString();
	}

	private void error(HttpServletResponse response, String requestId, int status,
			String code, String message) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), Map.of(
				"success", false,
				"code", code,
				"message", message,
				"requestId", requestId));
	}

	private void record(HttpServletRequest request, HttpServletResponse response,
			FmExternalApiPrincipal principal, String requestId, Date requestDate,
			long startedAt, String resultCode) {
		try {
			String endpointCode = endpointCode(request);
			accessLedger.record(principal, requestId, request.getHeader("traceparent"),
					endpointCode, FmExternalApiEndpointCatalog.requiredScope(endpointCode),
					request.getHeader("User-Agent"), response.getStatus(), resultCode,
					(System.nanoTime() - startedAt) / 1_000_000,
					request.getContentLengthLong(), contentLength(response),
					request.getHeader("Idempotency-Key"), requestDate);
		} catch (Exception exception) {
			LOGGER.error("Cannot append external API access log for request {}",
					requestId, exception);
		}
	}

	private String endpointCode(HttpServletRequest request) {
		return request.getServletPath().substring(BASE_PATH.length());
	}

	private long contentLength(HttpServletResponse response) {
		try {
			return Long.parseLong(response.getHeader("Content-Length"));
		} catch (NumberFormatException exception) {
			return -1;
		}
	}
}
