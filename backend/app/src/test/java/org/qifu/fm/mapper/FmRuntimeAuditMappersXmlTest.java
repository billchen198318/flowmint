package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmRuntimeAuditMappersXmlTest {

    @Test
    void loadsRuntimeAuditMapperWithMyBatisParser() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        String[] mapperNames = {
                "FmFormSnapshot",
                "FmTaskAction",
                "FmTaskAssignmentSnapshot",
                "FmTaskAssignmentSnapshotDtl"
        };
        for (String mapperName : mapperNames) {
            parse(configuration, "org/qifu/fm/mapper/" + mapperName + "Mapper.xml");
            String namespace = "org.qifu.fm.mapper." + mapperName + "Mapper.";
            assertTrue(configuration.hasStatement(namespace + "selectByPrimaryKey"));
            assertTrue(configuration.hasStatement(namespace + "selectListByParams"));
            assertTrue(configuration.hasStatement(namespace + "findPage"));
            assertTrue(configuration.hasStatement(namespace + "count"));
            assertTrue(configuration.hasStatement(namespace + "insert"));
            assertTrue(configuration.hasStatement(namespace + "update"));
            assertTrue(configuration.hasStatement(namespace + "delete"));
        }
        assertTrue(configuration.hasStatement(
                "org.qifu.fm.mapper.FmTaskAssignmentSnapshotMapper"
                        + ".selectNextResolutionSeq"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(
                    reader,
                    configuration,
                    resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
