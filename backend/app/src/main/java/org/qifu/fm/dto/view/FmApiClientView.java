package org.qifu.fm.dto.view;

import java.util.List;

public record FmApiClientView(
		String oid,
		String tenantId,
		String clientId,
		String clientCode,
		String clientName,
		String systemType,
		String description,
		List<String> allowedScopes,
		List<String> allowedProcessIds,
		List<String> allowedInitiatorAccounts,
		List<String> ipAllowlist,
		Integer rateLimitPerMinute,
		Integer dailyQuota,
		String status,
		Integer lockVersion) {
}
