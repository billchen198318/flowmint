package org.qifu.fm.dto.view;

import java.util.Date;

import org.qifu.fm.entity.FmTenantAccount;

public record FmTenantAccountView(String oid, String account, String isDefault, String status, Date effectiveFrom,
		Date effectiveTo) {
	public static FmTenantAccountView from(FmTenantAccount value) {
		return new FmTenantAccountView(value.getOid(), value.getAccount(), value.getIsDefault(), value.getStatus(),
				value.getEffectiveFrom(), value.getEffectiveTo());
	}
}