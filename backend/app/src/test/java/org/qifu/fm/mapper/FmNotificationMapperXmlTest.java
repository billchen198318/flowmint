package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmNotificationMapperXmlTest {

	@Test
	void loadsNotificationStatementsWithMyBatisParser() throws Exception {
		Configuration configuration = new Configuration();
		parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
		parse(configuration, "org/qifu/fm/mapper/FmNotificationMapper.xml");
		String namespace = "org.qifu.fm.mapper.FmNotificationMapper.";
		assertTrue(configuration.hasStatement(namespace + "selectByPrimaryKey"));
		assertTrue(configuration.hasStatement(namespace + "findInbox"));
		assertTrue(configuration.hasStatement(namespace + "countUnread"));
		assertTrue(configuration.hasStatement(namespace + "markRead"));
		assertTrue(configuration.hasStatement(namespace + "markAllRead"));
		assertTrue(configuration.hasStatement(namespace + "insertIfAbsent"));
		assertTrue(configuration.hasStatement(namespace + "findPendingEmail"));
		assertTrue(configuration.hasStatement(namespace + "markDelivered"));
	}

	private void parse(Configuration configuration, String resource) throws Exception {
		try (Reader reader = Resources.getResourceAsReader(resource)) {
			new XMLMapperBuilder(reader, configuration, resource,
					configuration.getSqlFragments()).parse();
		}
	}
}
