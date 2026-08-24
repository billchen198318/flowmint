package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmTaskParallelAddSignMapperXmlTest {

    @Test
    void loadsStandardAndConditionalStatements() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmTaskParallelAddSignMapper.xml");
        String namespace = "org.qifu.fm.mapper.FmTaskParallelAddSignMapper.";
        assertTrue(configuration.hasStatement(namespace + "selectByPrimaryKey"));
        assertTrue(configuration.hasStatement(namespace + "insert"));
        assertTrue(configuration.hasStatement(namespace + "incrementResult"));
        assertTrue(configuration.hasStatement(namespace + "completeWaiting"));
        assertTrue(configuration.hasStatement(namespace + "cancelWaiting"));
        assertTrue(configuration.hasStatement(namespace + "selectLatestByParentTask"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
