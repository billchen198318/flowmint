package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmEmployee;

class FmEmployeeMapperXmlTest {

    @Test
    void emptyReadableTenantSetCannotReturnAllEmployees() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmEmployeeMapper.xml");
        var count = configuration.getMappedStatement("org.qifu.fm.mapper.FmEmployeeMapper.count");

        assertTrue(count.getBoundSql(Map.of("tenantIds", List.of())).getSql().contains("AND 1 = 0"));
        assertTrue(count.getBoundSql(Map.of("tenantIds", List.of("T-1")))
                .getSql().contains("TENANT_ID IN"));
    }

    @Test
    void updateCanClearEmployeeEffectiveTo() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmEmployeeMapper.xml");
        FmEmployee employee = new FmEmployee();
        employee.setOid("E-1");
        employee.setEffectiveTo(null);

        String sql = configuration.getMappedStatement("org.qifu.fm.mapper.FmEmployeeMapper.update")
                .getBoundSql(employee).getSql();
        assertTrue(sql.contains("EFFECTIVE_TO = ?"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource,
                    configuration.getSqlFragments()).parse();
        }
    }
}
