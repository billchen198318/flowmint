package org.qifu.fm.domain.externalapi;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qifu.fm.entity.FmApiAccessLog;
import org.qifu.fm.service.IFmApiAccessLogService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FmExternalApiAccessLedger {

	private final IFmApiAccessLogService accessLogService;

	public FmExternalApiAccessLedger(IFmApiAccessLogService accessLogService) {
		this.accessLogService = accessLogService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class)
	public void record(FmExternalApiPrincipal principal, String requestId,
			String traceId, String endpointCode, String requiredScope,
			String userAgent, int httpStatus, String resultCode, long elapsedMillis,
			long requestBytes, long responseBytes, String idempotencyKey,
			Date requestDate) {
		FmApiAccessLog access = new FmApiAccessLog();
		access.setTenantId(principal.tenantId());
		access.setClientId(principal.clientId());
		access.setKeyId(principal.keyId());
		access.setSourceIp(StringUtils.abbreviate(principal.sourceIp(), 45));
		access.setCuserid(StringUtils.abbreviate(principal.clientCode(), 24));
		append(access, requestId, traceId, endpointCode, requiredScope, userAgent,
				httpStatus, resultCode, elapsedMillis, requestBytes, responseBytes,
				idempotencyKey, requestDate);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW,
			rollbackFor = Exception.class)
	public void recordAuthenticationFailure(String sourceIp, String requestId,
			String traceId, String endpointCode, String requiredScope,
			String userAgent, long elapsedMillis, long requestBytes,
			long responseBytes, String idempotencyKey, Date requestDate) {
		FmApiAccessLog access = new FmApiAccessLog();
		access.setSourceIp(StringUtils.abbreviate(
				StringUtils.defaultIfBlank(sourceIp, "UNKNOWN"), 45));
		access.setCuserid("UNAUTHENTICATED");
		append(access, requestId, traceId, endpointCode, requiredScope, userAgent,
				401, "AUTHENTICATION_FAILED", elapsedMillis, requestBytes,
				responseBytes, idempotencyKey, requestDate);
	}

	private void append(FmApiAccessLog access, String requestId, String traceId,
			String endpointCode, String requiredScope, String userAgent,
			int httpStatus, String resultCode, long elapsedMillis,
			long requestBytes, long responseBytes, String idempotencyKey,
			Date requestDate) {
		access.setAccessId(UUID.randomUUID().toString());
		access.setRequestId(StringUtils.abbreviate(requestId, 64));
		access.setTraceId(StringUtils.abbreviate(StringUtils.trimToNull(traceId), 64));
		access.setEndpointCode(StringUtils.abbreviate(endpointCode, 100));
		access.setRequiredScope(StringUtils.abbreviate(
				StringUtils.trimToNull(requiredScope), 100));
		access.setUserAgentSummary(StringUtils.abbreviate(
				StringUtils.trimToNull(userAgent), 255));
		access.setHttpStatus(httpStatus);
		access.setResultCode(StringUtils.abbreviate(resultCode, 50));
		access.setElapsedMillis(Math.max(0, elapsedMillis));
		access.setRequestBytes(requestBytes < 0 ? null : requestBytes);
		access.setResponseBytes(responseBytes < 0 ? null : responseBytes);
		access.setIdempotencyKeyHash(hash(StringUtils.trimToNull(idempotencyKey)));
		access.setRequestDate(requestDate);
		accessLogService.append(access);
	}

	private String hash(String value) {
		if (value == null) {
			return null;
		}
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is unavailable.", exception);
		}
	}
}
