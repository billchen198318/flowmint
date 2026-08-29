package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmApiClientMapperXmlTest {
	@Test
	void mapperXmlLoadsAndProvidesBaseStatements() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		parse(configuration, "org/qifu/fm/mapper/FmApiClientMapper.xml");
		for (String id : new String[] {"selectByPrimaryKey", "selectListByParams",
				"findPage", "count", "insert", "update", "delete"}) {
			assertTrue(configuration.hasStatement(
					"org.qifu.fm.mapper.FmApiClientMapper." + id));
		}
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
