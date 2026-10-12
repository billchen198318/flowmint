package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmFormVersionMapperXmlTest {

    @Test
    void draftStateIsReadUnderRowLock() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmFormVersionMapper.xml");
        String sql = configuration.getMappedStatement(
                "org.qifu.fm.mapper.FmFormVersionMapper.selectForUpdate")
                .getBoundSql(Map.of("oid", "draft-oid")).getSql();
        assertTrue(sql.contains("WHERE OID = ?"));
        assertTrue(sql.contains("FOR UPDATE"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
