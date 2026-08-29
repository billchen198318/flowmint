package org.qifu.fm.dto.command;

import java.util.List;

public record FmApiClientCommand(
		String oid,
		String tenantId,
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
