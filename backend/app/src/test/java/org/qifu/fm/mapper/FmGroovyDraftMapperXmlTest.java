package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.Reader;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.impl.FmProcessVersionServiceImpl;
import org.springframework.context.support.GenericApplicationContext;

class FmGroovyDraftMapperXmlTest {

    @Test
    void springLoadsVersionMapperAndServiceWithoutOpeningDatabaseConnection() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Configuration configuration = new Configuration(new Environment(
                "groovy-preview-test", new SpringManagedTransactionFactory(), dataSource));
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmProcessVersionMapper.xml");
        var sessionFactory = new DefaultSqlSessionFactory(configuration);
        try (var context = new GenericApplicationContext()) {
            context.registerBean("versionMapper", MapperFactoryBean.class, () -> {
                var mapper = new MapperFactoryBean<>(FmProcessVersionMapper.class);
                mapper.setSqlSessionFactory(sessionFactory);
                return mapper;
            });
            context.registerBean(IFmProcessVersionService.class,
                    () -> new FmProcessVersionServiceImpl(context.getBean(FmProcessVersionMapper.class)));
            context.refresh();
            assertNotNull(context.getBean(FmProcessVersionMapper.class));
            assertNotNull(context.getBean(IFmProcessVersionService.class));
            assertTrue(configuration.hasStatement("org.qifu.fm.mapper.FmProcessVersionMapper.findDraftLockVersion"));
            verifyNoInteractions(dataSource);
        }
    }

    @Test
    void loadsStatementsAndKeepsTenantDraftAndLockGuards() throws Exception {
        Configuration configuration = new Configuration();
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmProcessSystemTaskMapper.xml");
        parse(configuration, "org/qifu/fm/mapper/FmProcessVersionMapper.xml");
        String bindings = "org.qifu.fm.mapper.FmProcessSystemTaskMapper.";
        for (String statement : new String[] { "selectByPrimaryKey", "selectListByParams", "findPage",
                "count", "insert", "update", "delete", "deleteDraftVersion" }) {
            assertTrue(configuration.hasStatement(bindings + statement));
        }
        Map<String, Object> parameters = Map.of("tenantId", "T1", "processDefId", "P1",
                "versionNo", 1, "oid", "V1", "expectedLockVersion", 3);
        String deletion = configuration.getMappedStatement(bindings + "deleteDraftVersion")
                .getBoundSql(parameters).getSql();
        assertTrue(deletion.contains("TENANT_ID = ?"));
        assertTrue(deletion.contains("VERSION_STATUS = 'DRAFT'"));
        String advance = configuration.getMappedStatement(
                "org.qifu.fm.mapper.FmProcessVersionMapper.advanceDraftLock")
                .getBoundSql(parameters).getSql();
        assertTrue(advance.contains("LOCK_VERSION = ?"));
        assertTrue(advance.contains("TENANT_ID = ?"));
        assertTrue(advance.contains("VERSION_STATUS = 'DRAFT'"));
        String preview = configuration.getMappedStatement(
                "org.qifu.fm.mapper.FmProcessVersionMapper.findDraftLockVersion")
                .getBoundSql(parameters).getSql();
        assertTrue(preview.contains("TENANT_ID = ?"));
        assertTrue(preview.contains("VERSION_STATUS = 'DRAFT'"));
        assertFalse(preview.contains("FOR UPDATE"));
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }
}
