package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmOrgApprovalLevel;
import org.qifu.fm.entity.FmOrgLevelScheme;

class FmOrgLevelSchemeMapperXmlTest {
	@Test
	void nullableEndDateIsWrittenAndPageIsTenantScoped() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		parse(configuration, "org/qifu/fm/mapper/FmOrgLevelSchemeMapper.xml");
		parse(configuration, "org/qifu/fm/mapper/FmOrgApprovalLevelMapper.xml");

		String schemeUpdate = configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmOrgLevelSchemeMapper.update")
				.getBoundSql(new FmOrgLevelScheme()).getSql();
		String levelUpdate = configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmOrgApprovalLevelMapper.update")
				.getBoundSql(new FmOrgApprovalLevel()).getSql();
		assertTrue(schemeUpdate.contains("EFFECTIVE_TO = ?"));
		assertTrue(levelUpdate.contains("EFFECTIVE_TO = ?"));

		String scopedPage = configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmOrgLevelSchemeMapper.findPage")
				.getBoundSql(Map.of("readableTenantIds", Set.of("T"))).getSql();
		assertTrue(scopedPage.contains("TENANT_ID IN"));
		String emptyPage = configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmOrgLevelSchemeMapper.findPage")
				.getBoundSql(Map.of("readableTenantIds", Set.of())).getSql();
		assertTrue(emptyPage.contains("1 = 0"));
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
