package org.qifu.fm.dto.command;

import java.util.Date;

public record FmOrgLevelCommand(String oid, String levelCode, String levelName, Integer levelOrder,
		String isHighestLevel, String status, Date effectiveFrom, Date effectiveTo, String description) {
}
