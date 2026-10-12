package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.impl.el.DefaultExpressionManager;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.jupiter.api.Test;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.domain.resolver.IFmAssignmentResolverService;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.dto.view.FmResolverCandidateView;
import org.qifu.fm.dto.view.FmResolverPreviewView;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.logic.impl.FmProcessDefLogicServiceImpl;
import org.qifu.fm.service.IFmTaskActionService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;

import tools.jackson.databind.ObjectMapper;

class FmMultiInstanceIncidentEngineTest {

    @Test
    void failedPreflightCanRetryIntoAllAssignments() throws Exception {
        runRecovery("ALL", 2);
        runRecovery("SEQUENTIAL", 1);
    }

    private void runRecovery(String mode, int initialTaskCount) throws Exception {
        IFmTaskPolicyService policies = mock(IFmTaskPolicyService.class);
        IFmTaskAssignmentRuleService rules = mock(IFmTaskAssignmentRuleService.class);
        IFmAssignmentResolverService resolver = mock(IFmAssignmentResolverService.class);
        FmAssignmentIncidentRecorder incidents = mock(FmAssignmentIncidentRecorder.class);
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setTaskDefKey("approval");
        policy.setAssignmentMode(mode);
        policy.setSelfApprovalPolicy("ALLOW");
        policy.setDuplicatePolicy("KEEP_EACH_LEVEL");
        when(policies.findByVersion("T1", "P1", 1)).thenReturn(List.of(policy));
        when(rules.findByVersion("T1", "P1", 1)).thenReturn(List.of());
        when(incidents.record(any())).thenReturn("INC1");
        FmTaskAssignmentListener listener = spy(new FmTaskAssignmentListener(
                rules, policies, resolver, mock(IFmRuntimeAuditLogicService.class),
                incidents, mock(FmNotificationPublisher.class),
                mock(IFmTaskActionService.class), new ObjectMapper()));
        doNothing().when(listener).notify(any());

        FmProcessDefLogicServiceImpl publisher = new FmProcessDefLogicServiceImpl(
                null, null, null, null, null, policies, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                mock(FmDataActionTaskPublishValidator.class), null, null, false, null);
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        version.setBpmnXml("""
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  targetNamespace="FlowMint">
                  <process id="miIncidentTest" isExecutable="true">
                    <startEvent id="start" />
                    <userTask id="approval" name="Approval" />
                    <endEvent id="end" />
                    <sequenceFlow id="f1" sourceRef="start" targetRef="approval" />
                    <sequenceFlow id="f2" sourceRef="approval" targetRef="end" />
                  </process>
                </definitions>
                """);
        Method method = FmProcessDefLogicServiceImpl.class
                .getDeclaredMethod("runtimeBpmnXml", FmProcessVersion.class);
        method.setAccessible(true);
        String runtimeXml = (String) method.invoke(publisher, version);
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowmint-mi-incident-" + mode
                        + ";DB_CLOSE_DELAY=-1")
                .setDatabaseSchemaUpdate("true")
                .setAsyncExecutorActivate(false);
        configuration.setExpressionManager(new DefaultExpressionManager(Map.of(
                "fmTaskAssignmentListener", listener,
                "fmMultiInstancePreflightDelegate", new FmMultiInstancePreflightDelegate(listener),
                "fmMultiInstanceIncidentListener", new FmMultiInstanceIncidentListener(
                        incidents, new ObjectMapper()))));
        var engine = configuration.buildProcessEngine();
        try {
            engine.getRepositoryService().createDeployment()
                    .addString("miIncidentTest.bpmn20.xml", runtimeXml).deploy();
            var instance = engine.getRuntimeService().startProcessInstanceByKey(
                    "miIncidentTest", Map.of(
                            "flowmintTenantId", "T1",
                            "flowmintProcessDefId", "P1",
                            "flowmintProcessVersionNo", 1,
                            "flowmintInitiatorAccount", "applicant",
                            "flowmintFormDataId", "FD1"));
            var incidentTasks = engine.getTaskService().createTaskQuery()
                    .processInstanceId(instance.getId()).list();
            assertEquals(1, incidentTasks.size());
            assertEquals("approval__fmMiIncident",
                    incidentTasks.getFirst().getTaskDefinitionKey());
            assertEquals("FLOWMINT_INCIDENT:INC1", incidentTasks.getFirst().getCategory());

            FmTaskAssignmentRule rule = new FmTaskAssignmentRule();
            rule.setTaskDefKey("approval");
            rule.setStatus("ACTIVE");
            rule.setRuleSeq(1);
            rule.setResolverType("STATIC");
            when(rules.findByVersion("T1", "P1", 1)).thenReturn(List.of(rule));
            when(resolver.resolve(any(), any(), any())).thenReturn(
                    new FmResolverPreviewView("approval", 1, "STATIC", "RESOLVED", "",
                            List.of(new FmResolverCandidateView("E1", "alice", "Alice"),
                                    new FmResolverCandidateView("E2", "bob", "Bob"))));
            engine.getTaskService().complete(incidentTasks.getFirst().getId());
            var approvalTasks = engine.getTaskService().createTaskQuery()
                    .processInstanceId(instance.getId()).list();
            assertEquals(initialTaskCount, approvalTasks.size());
            approvalTasks.forEach(task -> {
                assertEquals("approval", task.getTaskDefinitionKey());
                assertNotNull(task.getId());
            });
            if ("SEQUENTIAL".equals(mode)) {
                String firstId = approvalTasks.getFirst().getId();
                engine.getTaskService().complete(firstId);
                var nextTasks = engine.getTaskService().createTaskQuery()
                        .processInstanceId(instance.getId()).list();
                assertEquals(1, nextTasks.size());
                assertEquals("approval", nextTasks.getFirst().getTaskDefinitionKey());
            }
        } finally {
            engine.close();
        }
    }
}
