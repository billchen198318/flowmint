package org.qifu.fm.dto.view;

import java.util.Date;

public record FmApiClientKeyView(
		String oid,
		String clientId,
		String keyId,
		String maskedKey,
		Date effectiveFrom,
		Date expiresAt,
		Date revokedAt,
		String revokedBy,
		String revokeReason,
		Date lastUsedAt,
		String lastSourceIp,
		String status) {
}
