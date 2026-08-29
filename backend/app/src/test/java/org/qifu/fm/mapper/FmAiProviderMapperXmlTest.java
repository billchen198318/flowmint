package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmAiProviderMapperXmlTest {

	@Test
	void loadsAllBaseMapperStatements() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		parse(configuration, "org/qifu/fm/mapper/FmAiProviderMapper.xml");

		String namespace = "org.qifu.fm.mapper.FmAiProviderMapper.";
		assertTrue(configuration.hasStatement(namespace + "selectByPrimaryKey"));
		assertTrue(configuration.hasStatement(namespace + "selectListByParams"));
		assertTrue(configuration.hasStatement(namespace + "findPage"));
		assertTrue(configuration.hasStatement(namespace + "count"));
		assertTrue(configuration.hasStatement(namespace + "insert"));
		assertTrue(configuration.hasStatement(namespace + "update"));
		assertTrue(configuration.hasStatement(namespace + "delete"));
		assertTrue(configuration.hasStatement(namespace + "lockByTenant"));
		assertTrue(configuration.hasStatement(namespace + "updateTestStatus"));
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
