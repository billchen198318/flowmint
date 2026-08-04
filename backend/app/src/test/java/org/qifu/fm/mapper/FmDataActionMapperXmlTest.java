package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmDataActionMapperXmlTest {

	@Test
	void loadsAllDataActionMappersWithMyBatisParser() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		List<String> resources = List.of(
				"org/qifu/fm/mapper/FmDataActionMapper.xml",
				"org/qifu/fm/mapper/FmDataActionVersionMapper.xml",
				"org/qifu/fm/mapper/FmDataActionStepMapper.xml");
		for (String resource : resources) {
			parse(configuration, resource);
		}
		assertTrue(configuration.hasStatement(
				"org.qifu.fm.mapper.FmDataActionMapper.selectByPrimaryKey"));
		assertTrue(configuration.hasStatement(
				"org.qifu.fm.mapper.FmDataActionVersionMapper.findPage"));
		assertTrue(configuration.hasStatement(
				"org.qifu.fm.mapper.FmDataActionStepMapper.insert"));
	}

	private void parse(Configuration configuration, String resource)
			throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
