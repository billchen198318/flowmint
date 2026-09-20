package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmGroovyDraftValidator;
import org.qifu.fm.domain.workflow.FmGroovyPreviewQuota;
import org.qifu.fm.domain.workflow.FmGroovyPreviewRunner;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeBpmn;
import org.qifu.fm.domain.workflow.FmGroovyPublishAccess;
import org.qifu.fm.logic.impl.FmGroovyPublishCoordinatorLogicServiceImpl;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessGroovyManifest;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.logic.impl.FmGroovyPublicationLogicServiceImpl;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessGroovyManifestService;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyPublicationLogicTest {

    private final IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
    private final IFmProcessDefService definitions = mock(IFmProcessDefService.class);
    private final IFmProcessSystemTaskService bindings = mock(IFmProcessSystemTaskService.class);
    private final IFmTaskFormRuleService rules = mock(IFmTaskFormRuleService.class);
    private final IFmFormVersionService forms = mock(IFmFormVersionService.class);
    private final IFmProcessGroovyManifestService manifests = mock(IFmProcessGroovyManifestService.class);
    private final FmGroovyPreviewRunner runner = mock(FmGroovyPreviewRunner.class);
    private final FmGroovyPublicationCompiler compiler = new FmGroovyPublicationCompiler(
            runner, new FmGroovyPreviewQuota());
    private final FmGroovyPublicationLogicServiceImpl logic = new FmGroovyPublicationLogicServiceImpl(
            versions, definitions, bindings, rules, forms, manifests);
    private final FmProcessVersion version = new FmProcessVersion();
    private final FmFormVersion form = new FmFormVersion();
    private final List<FmGroovyBindingCommand> commands = List.of(new FmGroovyBindingCommand(
            "calculate", "calculation", "return [:]", "{\"type\":\"object\",\"properties\":{}}",
            "{\"type\":\"object\",\"properties\":{}}", "{\"mappingVersion\":1,\"input\":{},\"output\":{}}", 3000));

    @BeforeEach
    void setup() throws Exception {
        version.setOid("version");
        version.setTenantId("T");
        version.setProcessDefId("P");
        version.setVersionNo(1);
        version.setVersionStatus("DRAFT");
        version.setBpmnXml("""
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                    xmlns:fm="https://flowmint.qifu.org/schema/bpmn" targetNamespace="test">
                    <process id="P" isExecutable="true">
                        <startEvent id="start" />
                        <serviceTask id="calculate" fm:taskType="GROOVY" fm:bindingId="calculation" />
                        <endEvent id="end" />
                        <sequenceFlow id="f1" sourceRef="start" targetRef="calculate" />
                        <sequenceFlow id="f2" sourceRef="calculate" targetRef="end" />
                    </process>
                </definitions>
                """);
        when(versions.lockDraft("T", "version")).thenReturn(4);
        when(versions.selectByPrimaryKey("version")).thenReturn(result(version));
        var definition = new FmProcessDef();
        definition.setTenantId("T");
        definition.setProcessDefId("P");
        definition.setProcessKey("P");
        when(definitions.selectListByParams(anyMap())).thenReturn(result(List.of(definition)));
        when(bindings.findVersion("T", "P", 1)).thenReturn(
                new FmGroovyDraftValidator().validate(version, version.getBpmnXml(), commands));
        var rule = new FmTaskFormRule();
        rule.setTenantId("T");
        rule.setProcessDefId("P");
        rule.setProcessVersionNo(1);
        rule.setFormId("F");
        rule.setFormVersionNo(1);
        when(rules.findByVersion("T", "P", 1)).thenReturn(List.of(rule));
        form.setTenantId("T");
        form.setFormId("F");
        form.setVersionNo(1);
        form.setVersionStatus("PUBLISHED");
        form.setSchemaContent("{\"components\":[]}");
        when(forms.selectListByParams(anyMap())).thenReturn(result(List.of(form)));
        when(manifests.insert(any())).thenAnswer(invocation -> result(invocation.getArgument(0)));
        when(runner.profile()).thenReturn(new FmGroovyVersionManifest.EngineProfile(
                "5.0.6", "ExampleJDK", "21.0.10+7", "local/worker@sha256:" + "a".repeat(64), "1"));
        when(runner.execute(any(), anyInt())).thenAnswer(invocation -> {
            var json = JsonMapper.builder().build();
            var request = json.readTree((byte[]) invocation.getArgument(0));
            assertEquals("CHECK", request.path("operation").asText());
            var response = json.createObjectNode();
            for (String field : List.of("protocolVersion", "profile", "invocationId", "attemptId",
                    "generation", "bindingSha256")) {
                response.set(field, request.get(field));
            }
            response.put("status", "VALID");
            return json.writeValueAsBytes(response);
        });
    }

    @Test
    void runtimeXmlPinsVerifiedBindingAndDisablesEngineRetries() throws Exception {
        var evidence = compiler.compile(version, "P", commands, form.getSchemaContent());
        var prepared = FmGroovyRuntimeBpmn.prepare(version.getBpmnXml(), evidence);
        String runtime = new String(new org.flowable.bpmn.converter.BpmnXMLConverter().convertToXML(prepared),
                java.nio.charset.StandardCharsets.UTF_8);
        var reader = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(
                new java.io.StringReader(runtime));
        var model = new org.flowable.bpmn.converter.BpmnXMLConverter().convertToBpmnModel(reader);
        reader.close();
        var task = (org.flowable.bpmn.model.ServiceTask) model.getMainProcess().getFlowElement("calculate");
        assertEquals("${fmGroovyTaskDelegate}", task.getImplementation());
        assertEquals(true, task.isAsynchronous());
        assertEquals(true, task.isExclusive());
        assertEquals("R0/PT1M", task.getFailedJobRetryTimeCycleValue());
        assertEquals(2, task.getFieldExtensions().size());
        var fields = task.getFieldExtensions().stream().collect(java.util.stream.Collectors.toMap(
                org.flowable.bpmn.model.FieldExtension::getFieldName,
                org.flowable.bpmn.model.FieldExtension::getStringValue));
        assertEquals("calculation", fields.get("flowmintBindingId"));
        assertEquals(evidence.manifest().bindings().getFirst().sha256(), fields.get("flowmintBindingSha256"));
        assertEquals("DRAFT", version.getVersionStatus());
        verifyNoInteractions(manifests);
    }

    @Test
    void runtimeXmlConvertsEveryGroovyNodeAndPreservesDataActionDesign() throws Exception {
        version.setBpmnXml(version.getBpmnXml().replace("<endEvent id=\"end\" />", """
                <serviceTask id="calculateAgain" fm:taskType="GROOVY" fm:bindingId="second" />
                <serviceTask id="loadData" fm:taskType="DATA_ACTION"
                    fm:actionCode="LOOKUP" fm:actionVersion="1"
                    fm:requestMapping="{}" fm:responseMapping="{}" />
                <endEvent id="end" />
                """).replace("sourceRef=\"calculate\" targetRef=\"end\"",
                        "sourceRef=\"calculate\" targetRef=\"calculateAgain\"")
                .replace("</process>", """
                        <sequenceFlow id="f3" sourceRef="calculateAgain" targetRef="loadData" />
                        <sequenceFlow id="f4" sourceRef="loadData" targetRef="end" />
                    </process>
                    """));
        var first = commands.getFirst();
        var second = new FmGroovyBindingCommand("calculateAgain", "second", first.scriptContent(),
                first.inputSchema(), first.outputSchema(), first.mappingContent(), first.timeoutMs());
        var evidence = compiler.compile(version, "P", List.of(first, second), form.getSchemaContent());
        var model = FmGroovyRuntimeBpmn.prepare(version.getBpmnXml(), evidence);
        for (String nodeId : List.of("calculate", "calculateAgain")) {
            var task = (org.flowable.bpmn.model.ServiceTask) model.getMainProcess().getFlowElement(nodeId);
            assertEquals("${fmGroovyTaskDelegate}", task.getImplementation());
            assertEquals(2, task.getFieldExtensions().size());
        }
        var dataAction = (org.flowable.bpmn.model.ServiceTask) model.getMainProcess().getFlowElement("loadData");
        assertEquals("LOOKUP", dataAction.getAttributeValue(
                "https://flowmint.qifu.org/schema/bpmn", "actionCode"));
        assertEquals(0, dataAction.getFieldExtensions().size());
        assertNotEquals("${fmGroovyTaskDelegate}", dataAction.getImplementation());

        var policies = mock(org.qifu.fm.service.IFmTaskPolicyService.class);
        when(policies.findByVersion("T", "P", 1)).thenReturn(List.of());
        var validator = mock(org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator.class);
        var publisher = new org.qifu.fm.logic.impl.FmProcessDefLogicServiceImpl(
                null, null, null, null, null, policies, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                validator, null, null, false, null);
        var method = publisher.getClass().getDeclaredMethod("runtimeBpmnXml", FmProcessVersion.class,
                FmGroovyPublicationCompiler.VerifiedPublication.class);
        method.setAccessible(true);
        String runtime = (String) method.invoke(publisher, version, evidence);
        var reader = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(
                new java.io.StringReader(runtime));
        var deployed = new org.flowable.bpmn.converter.BpmnXMLConverter().convertToBpmnModel(reader);
        reader.close();
        var deployedAction = (org.flowable.bpmn.model.ServiceTask) deployed.getMainProcess().getFlowElement("loadData");
        assertEquals("${fmDataActionTaskDelegate}", deployedAction.getImplementation());
        assertEquals("LOOKUP", deployedAction.getFieldExtensions().stream()
                .filter(field -> "flowmintActionCode".equals(field.getFieldName()))
                .findFirst().orElseThrow().getStringValue());
        for (String nodeId : List.of("calculate", "calculateAgain")) {
            var task = (org.flowable.bpmn.model.ServiceTask) deployed.getMainProcess().getFlowElement(nodeId);
            assertEquals("${fmGroovyTaskDelegate}", task.getImplementation());
            assertEquals(2, task.getFieldExtensions().size());
            assertEquals("R0/PT1M", task.getFailedJobRetryTimeCycleValue());
        }
    }

    @Test
    void runtimeXmlRejectsChangedDesignAndMissingEvidence() throws Exception {
        var evidence = compiler.compile(version, "P", commands, form.getSchemaContent());
        assertThrows(ServiceException.class,
                () -> FmGroovyRuntimeBpmn.prepare(version.getBpmnXml() + " ", evidence));
        assertThrows(ServiceException.class,
                () -> FmGroovyRuntimeBpmn.prepare(version.getBpmnXml(), null));
        assertThrows(ServiceException.class, () -> FmGroovyRuntimeBpmn.prepare(null, evidence));
    }

    @Test
    void persistsOnlyServerCompiledMatchingContent() throws Exception {
        var evidence = compiler.compile(version, "P", commands, form.getSchemaContent());
        logic.persist("T", "version", 4, evidence);
        var saved = ArgumentCaptor.forClass(FmProcessGroovyManifest.class);
        verify(manifests).insert(saved.capture());
        assertEquals(evidence.manifest().sha256(), saved.getValue().getManifestSha256());
        assertEquals(evidence.profile().canonical(), saved.getValue().getEngineProfile());
        assertEquals("DRAFT", version.getVersionStatus());
    }

    @Test
    void staleLockOrChangedXmlCannotSaveCompilationEvidence() throws Exception {
        var evidence = compiler.compile(version, "P", commands, form.getSchemaContent());
        assertThrows(ServiceException.class, () -> logic.persist("T", "version", 3, evidence));
        version.setBpmnXml(version.getBpmnXml() + " ");
        assertThrows(ServiceException.class, () -> logic.persist("T", "version", 4, evidence));
        verifyNoInteractions(manifests);
    }

    @Test
    void changedFormCannotReuseCompilationEvidence() throws Exception {
        var evidence = compiler.compile(version, "P", commands, form.getSchemaContent());
        form.setSchemaContent("{\"components\":[],\"title\":\"changed\"}");
        assertThrows(ServiceException.class, () -> logic.persist("T", "version", 4, evidence));
        verifyNoInteractions(manifests);
    }

    @Test
    void forgedWorkerReplyOrUnavailableRunnerProducesNoEvidence() throws Exception {
        doReturn("{\"status\":\"VALID\"}".getBytes()).when(runner).execute(any(), anyInt());
        assertThrows(ServiceException.class, () -> compiler.compile(version, "P", commands, form.getSchemaContent()));
        when(runner.profile()).thenThrow(new ServiceException("GROOVY_PREVIEW_UNAVAILABLE"));
        assertThrows(ServiceException.class, () -> compiler.compile(version, "P", commands, form.getSchemaContent()));
        verifyNoInteractions(manifests);
    }

    @Test
    void coordinatorCompilesOutsideTransactionAndCommitsPublicationWorkTogether() throws Exception {
        var manager = new RecordingTransactionManager();
        var access = mock(FmGroovyPublishAccess.class);
        var publication = mock(IFmGroovyPublicationLogicService.class);
        var coordinator = new FmGroovyPublishCoordinatorLogicServiceImpl(access, compiler, runner, publication, manager);
        var evidence = coordinator.compile(version, "P", commands, form.getSchemaContent());
        assertEquals(0, manager.begins);
        String outcome = coordinator.commit("T", "version", 4, evidence, () -> {
            assertEquals(true, org.springframework.transaction.support.TransactionSynchronizationManager
                    .isActualTransactionActive());
            verify(publication).persist("T", "version", 4, evidence);
            return "deployed";
        });
        assertEquals("deployed", outcome);
        assertEquals(1, manager.commits);
        assertEquals(0, manager.rollbacks);
    }

    @Test
    void coordinatorRollsBackCheckedDeploymentFailure() throws Exception {
        var manager = new RecordingTransactionManager();
        var publication = mock(IFmGroovyPublicationLogicService.class);
        var coordinator = new FmGroovyPublishCoordinatorLogicServiceImpl(mock(FmGroovyPublishAccess.class),
                compiler, runner, publication, manager);
        var evidence = coordinator.compile(version, "P", commands, form.getSchemaContent());
        var failure = new ServiceException("DEPLOYMENT_FAILED");
        assertEquals(failure, assertThrows(ServiceException.class, () ->
                coordinator.commit("T", "version", 4, evidence, () -> {
                    throw failure;
                })));
        verify(publication).persist("T", "version", 4, evidence);
        assertEquals(0, manager.commits);
        assertEquals(1, manager.rollbacks);
    }

    @Test
    void coordinatorRejectsProfileChangeBeforeManifestAndCompilationInsideTransaction() throws Exception {
        var manager = new RecordingTransactionManager();
        var publication = mock(IFmGroovyPublicationLogicService.class);
        var coordinator = new FmGroovyPublishCoordinatorLogicServiceImpl(mock(FmGroovyPublishAccess.class),
                compiler, runner, publication, manager);
        var evidence = coordinator.compile(version, "P", commands, form.getSchemaContent());
        when(runner.profile()).thenReturn(new FmGroovyVersionManifest.EngineProfile(
                "5.0.6", "ExampleJDK", "21.0.10+7", "local/worker@sha256:" + "b".repeat(64), "1"));
        assertThrows(ServiceException.class, () -> coordinator.commit("T", "version", 4, evidence,
                () -> {
                    throw new AssertionError("Must not deploy");
                }));
        verifyNoInteractions(publication);
        assertEquals(1, manager.rollbacks);
        assertThrows(ServiceException.class, () -> coordinator.transaction(() ->
                coordinator.compile(version, "P", commands, form.getSchemaContent())));
        assertEquals(2, manager.rollbacks);
    }

    @Test
    void coordinatorDoesNotDeployAfterStaleManifestAndRollsBackRuntimeFailure() throws Exception {
        var manager = new RecordingTransactionManager();
        var publication = mock(IFmGroovyPublicationLogicService.class);
        var coordinator = new FmGroovyPublishCoordinatorLogicServiceImpl(mock(FmGroovyPublishAccess.class),
                compiler, runner, publication, manager);
        var evidence = coordinator.compile(version, "P", commands, form.getSchemaContent());
        org.mockito.Mockito.doThrow(new ServiceException("STALE"))
                .when(publication).persist("T", "version", 4, evidence);
        assertThrows(ServiceException.class, () -> coordinator.commit("T", "version", 4, evidence, () -> {
            throw new AssertionError("Must not deploy stale content");
        }));
        assertEquals(1, manager.rollbacks);
        assertThrows(IllegalStateException.class, () -> coordinator.transaction(() -> {
            throw new IllegalStateException("Flowable deployment failed");
        }));
        assertEquals(2, manager.rollbacks);
        assertEquals(0, manager.commits);
    }

    @Test
    void springProxyRefusesPersistenceOutsidePublisherTransaction() {
        var manager = new AbstractPlatformTransactionManager() {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                throw new AssertionError("Must not create an independent transaction");
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                throw new AssertionError("No transaction to commit");
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
                throw new AssertionError("No transaction to roll back");
            }
        };
        var factory = new ProxyFactory(logic);
        factory.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        var proxy = (IFmGroovyPublicationLogicService) factory.getProxy();
        assertThrows(IllegalTransactionStateException.class, () -> proxy.persist("T", "version", 4, null));
        verifyNoInteractions(versions, definitions, bindings, rules, forms, manifests);
    }

    private static <T> DefaultResult<T> result(T value) {
        var result = new DefaultResult<T>();
        result.setValue(value);
        return result;
    }

    private static final class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        private static final long serialVersionUID = 1L;
        private int begins;
        private int commits;
        private int rollbacks;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            begins++;
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks++;
        }
    }
}
