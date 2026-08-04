package org.qifu.fm.domain.datasource;

import java.util.Map;

import org.qifu.base.exception.ServiceException;

public final class FmDataSourceDriverCatalog {

	private static final Map<String, String> DRIVERS = Map.of(
			"MARIADB", "org.mariadb.jdbc.Driver",
			"ORACLE", "oracle.jdbc.OracleDriver",
			"MSSQL", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
	private static final Map<String, String> URL_PREFIXES = Map.of(
			"MARIADB", "jdbc:mariadb:",
			"ORACLE", "jdbc:oracle:",
			"MSSQL", "jdbc:sqlserver:");

	private FmDataSourceDriverCatalog() {
	}

	public static String driverClass(String dbType) throws ServiceException {
		String driverClass = DRIVERS.get(dbType);
		if (driverClass == null) {
			throw new ServiceException();
		}
		return driverClass;
	}

	public static boolean urlMatches(String dbType, String jdbcUrl) {
		String prefix = URL_PREFIXES.get(dbType);
		return prefix != null && jdbcUrl != null && jdbcUrl.startsWith(prefix);
	}
}
