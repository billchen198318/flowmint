package org.qifu.fm.dto.view;

import java.util.Date;

public record FmApiKeyIssueView(
		String keyOid,
		String clientId,
		String keyId,
		String apiKey,
		Date effectiveFrom,
		Date expiresAt,
		String notice) {
}
