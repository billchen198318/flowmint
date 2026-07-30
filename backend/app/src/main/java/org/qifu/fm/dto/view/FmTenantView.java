package org.qifu.fm.dto.view;

import java.util.List;

import org.qifu.fm.entity.FmTenant;

public record FmTenantView(String oid, String tenantId, String tenantCode, String tenantName, String defaultLocale,
		String defaultTimezone, String status, String description, List<FmTenantAccountView> accounts) {
	public static FmTenantView from(FmTenant value, List<FmTenantAccountView> accounts) {
		return new FmTenantView(value.getOid(), value.getTenantId(), value.getTenantCode(), value.getTenantName(),
				value.getDefaultLocale(), value.getDefaultTimezone(), value.getStatus(), value.getDescription(),
				accounts);
	}
}