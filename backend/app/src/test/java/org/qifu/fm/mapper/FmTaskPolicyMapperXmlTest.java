package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmTaskPolicyMapperXmlTest {

    @Test
    void loadsSlaColumnsWithMyBatisParser() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmTaskPolicyMapper.xml");
        assertTrue(configuration.hasStatement(
                "org.qifu.fm.mapper.FmTaskPolicyMapper.selectByVersion"));
        assertTrue(configuration.hasStatement(
                "org.qifu.fm.mapper.FmTaskPolicyMapper.insert"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
