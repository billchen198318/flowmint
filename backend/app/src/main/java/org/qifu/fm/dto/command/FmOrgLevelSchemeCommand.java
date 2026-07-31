package org.qifu.fm.dto.command;

import java.util.Date;
import java.util.List;

public record FmOrgLevelSchemeCommand(String oid, String tenantId, String schemeCode, String schemeName,
		String isDefault, String status, Date effectiveFrom, Date effectiveTo, String description,
		List<FmOrgLevelCommand> levels) {
}
