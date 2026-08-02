package org.qifu.fm.dto.command;

import java.util.Date;

public record FmWorkflowDelegationCommand(
		String oid,
		String tenantId,
		String principalAccount,
		String delegateAccount,
		String scopeType,
		String scopeRefId,
		String allowRedelegate,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String reason) {
}
