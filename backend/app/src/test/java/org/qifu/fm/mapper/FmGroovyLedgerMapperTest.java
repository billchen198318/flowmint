package org.qifu.fm.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.Reader;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.service.impl.FmSystemTaskExecutionServiceImpl;
import org.qifu.fm.service.impl.FmSystemTaskAttemptServiceImpl;
import org.qifu.fm.service.impl.FmProcessGroovyManifestServiceImpl;
import org.qifu.fm.service.impl.FmSystemTaskIncidentActionServiceImpl;
import org.springframework.context.support.GenericApplicationContext;

class FmGroovyLedgerMapperTest {

    @Test
    void springLoadsAllLedgerMappersAndServicesWithoutDatabaseConnection() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Configuration configuration = new Configuration(new Environment(
                "groovy-ledger-test", new SpringManagedTransactionFactory(), dataSource));
        parse(configuration, "org/qifu/core/mapper/DB1Config.xml");
        parse(configuration, "org/qifu/fm/mapper/FmProcessInstanceMapper.xml");
        parse(configuration, "org/qifu/fm/mapper/FmFormDataMapper.xml");
        for (String name : new String[] { "FmSystemTaskExecution", "FmSystemTaskAttempt", "FmProcessGroovyManifest",
                "FmSystemTaskIncidentAction" }) {
            parse(configuration, "org/qifu/fm/mapper/" + name + "Mapper.xml");
            for (String statement : new String[] { "selectByPrimaryKey", "selectListByParams", "findPage",
                    "count", "insert", "update", "delete" }) {
                assertTrue(configuration.hasStatement("org.qifu.fm.mapper." + name + "Mapper." + statement));
            }
        }
        var factory = new DefaultSqlSessionFactory(configuration);
        try (var context = new GenericApplicationContext()) {
            context.registerBean("executionMapper", MapperFactoryBean.class, () -> {
                var mapper = new MapperFactoryBean<>(FmSystemTaskExecutionMapper.class);
                mapper.setSqlSessionFactory(factory);
                return mapper;
            });
            context.registerBean("attemptMapper", MapperFactoryBean.class, () -> {
                var mapper = new MapperFactoryBean<>(FmSystemTaskAttemptMapper.class);
                mapper.setSqlSessionFactory(factory);
                return mapper;
            });
            context.registerBean("manifestMapper", MapperFactoryBean.class, () -> {
                var mapper = new MapperFactoryBean<>(FmProcessGroovyManifestMapper.class);
                mapper.setSqlSessionFactory(factory);
                return mapper;
            });
            context.registerBean("incidentActionMapper", MapperFactoryBean.class, () -> {
                var mapper = new MapperFactoryBean<>(FmSystemTaskIncidentActionMapper.class);
                mapper.setSqlSessionFactory(factory);
                return mapper;
            });
            context.registerBean(FmSystemTaskIncidentActionServiceImpl.class,
                    () -> new FmSystemTaskIncidentActionServiceImpl(context.getBean(FmSystemTaskIncidentActionMapper.class)));
            context.registerBean(FmSystemTaskExecutionServiceImpl.class,
                    () -> new FmSystemTaskExecutionServiceImpl(context.getBean(FmSystemTaskExecutionMapper.class)));
            context.registerBean(FmSystemTaskAttemptServiceImpl.class,
                    () -> new FmSystemTaskAttemptServiceImpl(context.getBean(FmSystemTaskAttemptMapper.class)));
            context.registerBean(FmProcessGroovyManifestServiceImpl.class,
                    () -> new FmProcessGroovyManifestServiceImpl(context.getBean(FmProcessGroovyManifestMapper.class)));
            context.refresh();
            assertNotNull(context.getBean(FmSystemTaskExecutionServiceImpl.class));
            assertNotNull(context.getBean(FmSystemTaskAttemptServiceImpl.class));
            assertNotNull(context.getBean(FmProcessGroovyManifestServiceImpl.class));
            assertNotNull(context.getBean(FmSystemTaskIncidentActionServiceImpl.class));
            verifyNoInteractions(dataSource);
        }
        String prefix = "org.qifu.fm.mapper.FmSystemTaskExecutionMapper.";
        Map<String, Object> parameters = Map.of("tenantId", "T", "oid", "E", "generation", 2,
                "now", new Date(1000), "leaseUntil", new Date(2000));
        String claim = configuration.getMappedStatement(prefix + "claim").getBoundSql(parameters).getSql();
        assertTrue(claim.contains("STATUS = 'READY'"));
        assertTrue(claim.contains("TENANT_ID = ?"));
        assertTrue(claim.contains("GENERATION = ?"));
        assertTrue(claim.contains("ATTEMPT_NO BETWEEN 0 AND 2"));
        String complete = configuration.getMappedStatement(prefix + "complete").getBoundSql(parameters).getSql();
        assertTrue(complete.contains("STATUS = 'RUNNING'"));
        assertTrue(complete.contains("TENANT_ID = ?"));
        assertTrue(complete.contains("GENERATION = ?"));
        assertTrue(complete.contains("LEASE_UNTIL > ?"));
        String cancel = configuration.getMappedStatement(prefix + "cancel").getBoundSql(parameters).getSql();
        assertTrue(cancel.contains("GENERATION = GENERATION + 1"));
        String expire = configuration.getMappedStatement(prefix + "expire").getBoundSql(parameters).getSql();
        assertTrue(expire.contains("TENANT_ID = ?"));
        assertTrue(expire.contains("GENERATION = ?"));
        assertTrue(expire.contains("TASK_TYPE = 'GROOVY'"));
        assertTrue(expire.contains("STATUS = 'RUNNING'"));
        assertTrue(expire.contains("LEASE_UNTIL <= ?"));
        String scan = configuration.getMappedStatement(prefix + "findExpired")
                .getBoundSql(Map.of("tenantId", "T", "now", new Date(), "limit", 100)).getSql();
        assertTrue(scan.contains("TENANT_ID = ?"));
        assertTrue(scan.contains("LIMIT ?"));
        String abandon = configuration.getMappedStatement("org.qifu.fm.mapper.FmSystemTaskAttemptMapper.finish")
                .getBoundSql(Map.of("status", "ABANDONED")).getSql();
        assertTrue(abandon.contains("LEASE_UNTIL <= ?"));
        String processLock = configuration.getMappedStatement("org.qifu.fm.mapper.FmProcessInstanceMapper.lockInstance")
                .getBoundSql(Map.of("tenantId", "T", "processInstanceId", "P")).getSql();
        assertTrue(processLock.contains("TENANT_ID = ?"));
        assertTrue(processLock.contains("FOR UPDATE"));
        String activeLock = configuration.getMappedStatement(prefix + "lockActiveForProcess")
                .getBoundSql(Map.of("tenantId", "T", "processInstanceId", "P")).getSql();
        assertTrue(activeLock.contains("TASK_TYPE = 'GROOVY'"));
        assertTrue(activeLock.contains("FOR UPDATE"));
        String cancelAttempt = configuration.getMappedStatement("org.qifu.fm.mapper.FmSystemTaskAttemptMapper.cancelRunning")
                .getBoundSql(parameters).getSql();
        assertTrue(cancelAttempt.contains("TENANT_ID = ?"));
        assertTrue(cancelAttempt.contains("GENERATION = ?"));
        assertTrue(cancelAttempt.contains("STATUS = 'RUNNING'"));
        String incidents = configuration.getMappedStatement(prefix + "findIncidents")
                .getBoundSql(Map.of("tenantId", "T", "invocationId", "I", "offset", 0, "limit", 51)).getSql();
        assertTrue(incidents.contains("TENANT_ID = ?"));
        assertTrue(incidents.contains("INVOCATION_ID = ?"));
        assertTrue(incidents.contains("TASK_TYPE = 'GROOVY'"));
        assertTrue(!incidents.contains("INPUT_CONTENT") && !incidents.contains("CONTEXT_CONTENT"));
        assertTrue(configuration.getMappedStatement(prefix + "fail").getBoundSql(parameters).getSql()
                .contains("INCIDENT_OID = COALESCE(INCIDENT_OID, OID)"));
        assertTrue(expire.contains("INCIDENT_OID = COALESCE(INCIDENT_OID, OID)"));
        String history = configuration.getMappedStatement("org.qifu.fm.mapper.FmSystemTaskIncidentActionMapper.history")
                .getBoundSql(Map.of("tenantId", "T", "executionOid", "E", "offset", 0, "limit", 1,
                        "locking", true, "requestId", "R")).getSql();
        assertTrue(history.contains("TENANT_ID = ?"));
        assertTrue(history.contains("EXECUTION_OID = ?"));
        assertTrue(history.contains("REQUEST_ID = ?"));
        assertTrue(history.contains("FOR UPDATE"));
        String rearm = configuration.getMappedStatement(prefix + "rearm").getBoundSql(parameters).getSql();
        assertTrue(rearm.contains("TENANT_ID = ?") && rearm.contains("GENERATION = ?"));
        assertTrue(rearm.contains("STATUS = 'FAILED'") && rearm.contains("ATTEMPT_NO BETWEEN 0 AND 2"));
        assertTrue(rearm.contains("NOT EXISTS") && rearm.contains("a.STATUS = 'RUNNING'"));
        assertTrue(rearm.contains("RESULT_SHA256 IS NULL"));
        assertTrue(!rearm.contains("INPUT_CONTENT =") && !rearm.contains("BINDING_SHA256 ="));
        assertTrue(incidents.contains("OR INCIDENT_OID IS NOT NULL"));
        assertTrue(incidents.contains("OR PARENT_INVOCATION_ID IS NOT NULL"));
        String formLock = configuration.getMappedStatement("org.qifu.fm.mapper.FmFormDataMapper.lockStateByFormDataId")
                .getBoundSql(Map.of("tenantId", "T", "formDataId", "F")).getSql();
        assertTrue(formLock.contains("TENANT_ID = ?") && formLock.contains("FORM_DATA_ID = ?"));
        assertTrue(formLock.contains("REVISION_NO") && formLock.contains("LOCK_VERSION") && formLock.contains("FOR UPDATE"));
        var action = new org.qifu.fm.entity.FmSystemTaskIncidentAction();
        action.setActionType("RECALCULATE");
        action.setTargetInvocationId("successor");
        action.setRequestSha256("a".repeat(64));
        String actionInsert = configuration.getMappedStatement("org.qifu.fm.mapper.FmSystemTaskIncidentActionMapper.insert")
                .getBoundSql(action).getSql();
        assertTrue(actionInsert.contains("TARGET_INVOCATION_ID") && actionInsert.contains("REQUEST_SHA256"));
        var readyParams = Map.of("tenantId", "T", "oid", "E", "generation", 0, "cutoff", new Date(),
                "now", new Date(), "limit", 100, "afterOid", "cursor", "status", "FAILED", "errorCode", "GROOVY_START_FAILED");
        String readyScan = configuration.getMappedStatement(prefix + "findStalledReady").getBoundSql(readyParams).getSql();
        assertTrue(readyScan.contains("TENANT_ID = ?") && readyScan.contains("STATUS = 'READY'"));
        assertTrue(readyScan.contains("COALESCE(NEXT_ATTEMPT_AT, STARTED_AT) <= ?"));
        assertTrue(readyScan.contains("OID > ?") && readyScan.contains("LIMIT ?"));
        assertTrue(!readyScan.contains("INPUT_CONTENT") && !readyScan.contains("CONTEXT_CONTENT"));
        String finishReady = configuration.getMappedStatement(prefix + "finishReady").getBoundSql(readyParams).getSql();
        assertTrue(finishReady.contains("TENANT_ID = ?") && finishReady.contains("GENERATION = ?"));
        assertTrue(finishReady.contains("STATUS = 'READY'") && finishReady.contains("RESULT_SHA256 IS NULL"));
        assertTrue(finishReady.contains("NOT EXISTS") && finishReady.contains("a.STATUS = 'RUNNING'"));
        assertTrue(finishReady.contains("GENERATION = GENERATION + 1") && !finishReady.contains("ATTEMPT_NO ="));
        assertTrue(rearm.contains("NEXT_ATTEMPT_AT = ?"));
        var retryParams = Map.of("tenantId", "T", "firstCutoff", new Date(1000),
                "secondCutoff", new Date(5000), "afterOid", "cursor", "limit", 100);
        String retryScan = configuration.getMappedStatement(prefix + "findAutomaticRetries")
                .getBoundSql(retryParams).getSql();
        assertTrue(retryScan.contains("TENANT_ID = ?") && retryScan.contains("STATUS = 'FAILED'"));
        assertTrue(retryScan.contains("ATTEMPT_NO = 1 AND COMPLETED_AT <= ?"));
        assertTrue(retryScan.contains("ATTEMPT_NO = 2 AND COMPLETED_AT <= ?"));
        assertTrue(retryScan.contains("GROOVY_RUNTIME_BUSY") && retryScan.contains("GROOVY_RUNTIME_START_UNAVAILABLE"));
        assertTrue(retryScan.contains("OID > ?") && retryScan.contains("LIMIT ?"));
        assertTrue(!retryScan.contains("INPUT_CONTENT") && !retryScan.contains("CONTEXT_CONTENT"));
    }

    @Test
    void automaticRetryQueryUsesSeparateBackoffCutoffsAndSystemAuditIdentity() throws Exception {
        var mapper = mock(FmSystemTaskExecutionMapper.class);
        var service = new FmSystemTaskExecutionServiceImpl(mapper);
        var now = new Date(10000);
        service.findAutomaticRetries("T", now, "cursor", 100);
        var capture = org.mockito.ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(mapper).findAutomaticRetries(capture.capture());
        assertTrue(new Date(9000).equals(capture.getValue().get("firstCutoff")));
        assertTrue(new Date(5000).equals(capture.getValue().get("secondCutoff")));
        assertTrue("T".equals(capture.getValue().get("tenantId")));
        var audit = new FmSystemTaskIncidentActionServiceImpl(mock(FmSystemTaskIncidentActionMapper.class));
        assertTrue("SYSTEM_TASK".equals(audit.getAccountId()));
    }

    @Test
    void genericMutationAndInvalidLeaseNeverReachMapper() throws Exception {
        var mapper = mock(FmSystemTaskExecutionMapper.class);
        var service = new FmSystemTaskExecutionServiceImpl(mapper);
        var row = new FmSystemTaskExecution();
        assertThrows(ServiceException.class, () -> service.update(row));
        assertThrows(ServiceException.class, () -> service.delete(row));
        assertThrows(ServiceException.class, () -> service.insert(row));
        assertThrows(ServiceException.class, () -> service.claim("T", "E", -1, new Date(1000), new Date(2000)));
        assertThrows(ServiceException.class, () -> service.claim("T", "E", 0, new Date(1000), new Date(999)));
        assertThrows(ServiceException.class, () -> service.complete("T", "E", 1, new Date(), "bad hash", null));
        assertThrows(ServiceException.class, () -> service.cancel("", "E", new Date()));
        assertThrows(ServiceException.class, () -> service.findExpired("", new Date(), 100));
        assertThrows(ServiceException.class, () -> service.findExpired("T", new Date(), 101));
        assertThrows(ServiceException.class, () -> service.expire("T", "E", 0, new Date()));
        assertThrows(ServiceException.class, () -> service.findStalledReady("T", new Date(), null, 101));
        assertThrows(ServiceException.class, () -> service.findAutomaticRetries("T", new Date(), null, 101));
        assertThrows(ServiceException.class, () -> service.findAutomaticRetries("", new Date(), null, 100));
        assertThrows(ServiceException.class, () -> service.finishReady("T", "E", 0, new Date(), new Date(), "SUCCEEDED", "GROOVY_START_FAILED"));
        assertThrows(ServiceException.class, () -> service.finishReady("T", "E", -1, new Date(), new Date(), "FAILED", "GROOVY_START_FAILED"));
        verifyNoInteractions(mapper);
    }

    private void parse(Configuration configuration, String resource) throws Exception {
        try (Reader reader = Resources.getResourceAsReader(resource)) {
            new XMLMapperBuilder(reader, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }
}
