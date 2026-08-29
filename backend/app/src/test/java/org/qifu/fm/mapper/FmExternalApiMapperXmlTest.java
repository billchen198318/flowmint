package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmExternalApiMapperXmlTest {

	@Test
	void keyMapperProvidesBaseAndLifecycleStatements() throws Exception {
		Configuration configuration = configuration();
		parse(configuration, "org/qifu/fm/mapper/FmApiClientKeyMapper.xml");
		assertStatements(configuration, "FmApiClientKeyMapper", "selectByPrimaryKey",
				"selectListByParams", "findPage", "count", "insert", "update",
				"delete", "selectByKeyId", "revoke", "markUsed");
	}

	@Test
	void accessLogMapperProvidesAppendOnlyCompatibleBaseStatements() throws Exception {
		Configuration configuration = configuration();
		parse(configuration, "org/qifu/fm/mapper/FmApiAccessLogMapper.xml");
		assertStatements(configuration, "FmApiAccessLogMapper", "selectByPrimaryKey",
				"selectListByParams", "findPage", "count", "insert", "update", "delete",
				"countClientRequestsSince");
	}

	@Test
	void requestMapperProvidesIdempotencyAndResultStatements() throws Exception {
		Configuration configuration = configuration();
		parse(configuration, "org/qifu/fm/mapper/FmApiRequestMapper.xml");
		assertStatements(configuration, "FmApiRequestMapper", "selectByPrimaryKey",
				"selectListByParams", "findPage", "count", "insert", "update", "delete",
				"selectByIdempotency", "selectByExternalReference", "updateResult");
	}

	private Configuration configuration() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		return configuration;
	}

	private void assertStatements(Configuration configuration, String mapper,
			String... statements) {
		for (String statement : statements) {
			assertTrue(configuration.hasStatement(
					"org.qifu.fm.mapper." + mapper + "." + statement));
		}
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
