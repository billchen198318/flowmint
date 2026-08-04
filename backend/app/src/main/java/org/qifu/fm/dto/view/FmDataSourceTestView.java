package org.qifu.fm.dto.view;

public record FmDataSourceTestView(
		boolean connected,
		String databaseProduct,
		String databaseVersion,
		String driverName,
		long elapsedMs) {
}
