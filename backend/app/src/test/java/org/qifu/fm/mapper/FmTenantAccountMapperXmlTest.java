package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmTenantAccount;

class FmTenantAccountMapperXmlTest {

    @Test
    void updateCanClearEffectiveTo() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmTenantAccountMapper.xml");
        FmTenantAccount membership = new FmTenantAccount();
        membership.setOid("M-1");
        membership.setEffectiveTo(null);

        String sql = configuration.getMappedStatement("org.qifu.fm.mapper.FmTenantAccountMapper.update")
                .getBoundSql(membership).getSql();
        assertTrue(sql.contains("EFFECTIVE_TO = ?"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
