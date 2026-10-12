package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmWorkflowDelegationMapperXmlTest {

	@Test
	void overlapQueryLocksRowsAndHandlesAllScope() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		parse(configuration, "org/qifu/fm/mapper/FmWorkflowDelegationMapper.xml");
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", "tenant");
		params.put("principalAccount", "principal");
		params.put("scopeType", "ALL");
		params.put("scopeRefId", null);
		params.put("effectiveFrom", new Date(10));
		params.put("effectiveTo", new Date(20));
		String sql = configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmWorkflowDelegationMapper.selectOverlappingActiveForUpdate")
				.getBoundSql(params).getSql();
		assertTrue(sql.contains("SCOPE_REF_ID IS NULL"));
		assertTrue(sql.contains("EFFECTIVE_FROM < ?"));
		assertTrue(sql.contains("EFFECTIVE_TO > ?"));
		assertTrue(sql.contains("FOR UPDATE"));
		assertTrue(configuration.getMappedStatement(
				"org.qifu.fm.mapper.FmWorkflowDelegationMapper.lockTenantForWrite")
				.getBoundSql(params).getSql().contains("FOR UPDATE"));
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
