package org.qifu.fm.domain.datasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FmDataSourceDriverCatalogTest {

	@Test
	void mapsTheThreeSupportedDrivers() throws Exception {
		assertEquals("org.mariadb.jdbc.Driver",
				FmDataSourceDriverCatalog.driverClass("MARIADB"));
		assertEquals("oracle.jdbc.OracleDriver",
				FmDataSourceDriverCatalog.driverClass("ORACLE"));
		assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver",
				FmDataSourceDriverCatalog.driverClass("MSSQL"));
	}

	@Test
	void validatesJdbcUrlAgainstDatabaseType() {
		assertTrue(FmDataSourceDriverCatalog.urlMatches(
				"MARIADB", "jdbc:mariadb://localhost:3306/test"));
		assertTrue(FmDataSourceDriverCatalog.urlMatches(
				"ORACLE", "jdbc:oracle:thin:@localhost:1521/FREEPDB1"));
		assertTrue(FmDataSourceDriverCatalog.urlMatches(
				"MSSQL", "jdbc:sqlserver://localhost:1433;databaseName=test"));
		assertFalse(FmDataSourceDriverCatalog.urlMatches(
				"ORACLE", "jdbc:mariadb://localhost:3306/test"));
	}
}
