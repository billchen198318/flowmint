package org.qifu.fm.dto.external;

import java.util.List;

public record FmExternalDepartmentTreeView(
		String format,
		long totalElements,
		int totalPages,
		int page,
		int pageSize,
		List<Node> nodes,
		List<FmExternalDepartmentView> items) {

	public record Node(FmExternalDepartmentView department, List<Node> children) { }
}
