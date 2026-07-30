package org.qifu.fm.dto.command;

import java.util.Date;

public record FmTenantAccountCommand(
		String oid,
		String tenantOid,
		String account,
		Boolean createNewAccount,
		String password,
		String confirmPassword,
		String isDefault,
		String status,
		Date effectiveFrom,
		Date effectiveTo) {
}