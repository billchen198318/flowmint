package org.qifu.fm.domain.externalapi;

import java.util.Set;

public record FmExternalApiPrincipal(
		String tenantId,
		String clientId,
		String clientCode,
		String keyId,
		Set<String> scopes,
		Set<String> allowedProcessIds,
		Set<String> allowedInitiatorAccounts,
		int rateLimitPerMinute,
		int dailyQuota,
		String sourceIp) {

	public boolean hasScope(String scope) {
		return scopes.contains(scope);
	}

	public boolean allowsProcess(String processId) {
		return allowedProcessIds.isEmpty() || allowedProcessIds.contains(processId);
	}

	public boolean allowsInitiator(String account) {
		return allowedInitiatorAccounts.isEmpty()
				|| allowedInitiatorAccounts.contains(account);
	}
}
