package org.qifu.fm.dto.view;

import java.util.Date;

public record FmWorkflowDelegationView(
		String oid,
		String tenantId,
		String delegationId,
		String principalAccount,
		String principalLabel,
		String delegateAccount,
		String delegateLabel,
		String scopeType,
		String scopeRefId,
		String scopeLabel,
		String allowRedelegate,
		String status,
		Date effectiveFrom,
		Date effectiveTo,
		String reason) {
}
