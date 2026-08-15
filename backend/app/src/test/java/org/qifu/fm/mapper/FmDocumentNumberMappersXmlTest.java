package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class FmDocumentNumberMappersXmlTest {

    private static final String RESOURCE_PREFIX = "org/qifu/fm/mapper/";

    @Test
    void loadsDocumentNumberMappersWithStandardStatements() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");

        assertStandardStatements(configuration, "FmDocumentNumberRule");
        assertStandardStatements(configuration, "FmDocumentSequence");
    }

    @Test
    void keepsCustomStatementsAfterStandardDeleteAndUsesTenantScopedParameters() throws Exception {
        String ruleXml = read("FmDocumentNumberRuleMapper.xml");
        assertAfter(ruleXml, "<delete id=\"delete\"", "<select id=\"selectActive\"");
        assertTrue(ruleXml.contains("TENANT_ID = #{tenantId,jdbcType=VARCHAR}"));
        assertTrue(ruleXml.contains("DOCUMENT_TYPE = #{documentType,jdbcType=VARCHAR}"));

        String sequenceXml = read("FmDocumentSequenceMapper.xml");
        assertAfter(sequenceXml, "<delete id=\"delete\"", "<select id=\"selectForUpdate\"");
        assertAfter(sequenceXml, "<delete id=\"delete\"", "<insert id=\"insertInitial\"");
        assertAfter(sequenceXml, "<delete id=\"delete\"", "<update id=\"increment\"");
        assertTrue(sequenceXml.contains("ON DUPLICATE KEY UPDATE OID = OID"));
        assertTrue(sequenceXml.contains("WHERE TENANT_ID = #{tenantId,jdbcType=VARCHAR}"));
        assertTrue(sequenceXml.contains("AND LOCK_VERSION = #{lockVersion,jdbcType=BIGINT}"));
    }

    private void assertStandardStatements(Configuration configuration, String mapperName) throws Exception {
        parse(configuration, RESOURCE_PREFIX + mapperName + "Mapper.xml");
        String namespace = "org.qifu.fm.mapper." + mapperName + "Mapper.";
        String[] statementNames = {
                "selectByPrimaryKey",
                "selectListByParams",
                "findPage",
                "count",
                "insert",
                "update",
                "delete"
        };
        for (String statementName : statementNames) {
            assertTrue(configuration.hasStatement(namespace + statementName));
        }
    }

    private void assertAfter(String xml, String earlier, String later) {
        assertTrue(xml.indexOf(earlier) >= 0);
        assertTrue(xml.indexOf(later) > xml.indexOf(earlier));
    }

    private String read(String mapperFile) throws Exception {
        try (var input = Resources.getResourceAsStream(RESOURCE_PREFIX + mapperFile)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
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
