package org.qifu.fm.dto.view;

import java.util.List;

public record FmOrgUnitMovePreviewView(
		String orgUnitId,
		String unitName,
		String oldParentOrgUnitId,
		String newParentOrgUnitId,
		Integer oldTreeDepth,
		Integer newTreeDepth,
		Integer affectedNodeCount,
		List<String> warnings) {
}
