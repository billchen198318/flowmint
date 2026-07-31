package org.qifu.fm.dto.view;

import java.util.Date;

import org.qifu.fm.entity.FmEmployee;

public record FmEmployeeView(String oid, String tenantId, String employeeId, String employeeNo, String account,
		String displayName, String email, String mobile, String locale, String timezone, String status,
		Date effectiveFrom, Date effectiveTo, String description) {
	public static FmEmployeeView from(FmEmployee value) {
		return new FmEmployeeView(value.getOid(), value.getTenantId(), value.getEmployeeId(), value.getEmployeeNo(),
				value.getAccount(), value.getDisplayName(), value.getEmail(), value.getMobile(), value.getLocale(),
				value.getTimezone(), value.getStatus(), value.getEffectiveFrom(), value.getEffectiveTo(),
				value.getDescription());
	}
}
