package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.impl.FmGroovyRuntimeInputLogicServiceImpl;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmProcessVersionService;

class FmGroovyRuntimeInputLogicTest {

    private final RuntimeService runtime = mock(RuntimeService.class);
    private final RepositoryService repository = mock(RepositoryService.class);
    private final IFmProcessInstanceService instances = mock(IFmProcessInstanceService.class);
    private final IFmFormDataService forms = mock(IFmFormDataService.class);
    private final IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
    private final IFmGroovyRuntimeDefinitionLogicService definitions = mock(IFmGroovyRuntimeDefinitionLogicService.class);
    private final Execution execution = mock(Execution.class);
    private final ProcessInstance engineInstance = mock(ProcessInstance.class);
    private final FmProcessInstance instance = new FmProcessInstance();
    private final FmFormData form = new FmFormData();
    private final FmProcessVersion version = new FmProcessVersion();
    private final ServiceTask task = new ServiceTask();
    private final FmGroovyRuntimeInputLogicServiceImpl logic = new FmGroovyRuntimeInputLogicServiceImpl(
            runtime, repository, instances, forms, versions, definitions);

    @BeforeEach
    void fixture() throws Exception {
        var query = mock(ExecutionQuery.class);
        when(runtime.createExecutionQuery()).thenReturn(query);
        when(query.executionId("execution")).thenReturn(query);
        when(query.singleResult()).thenReturn(execution);
        when(execution.getId()).thenReturn("execution");
        when(execution.getTenantId()).thenReturn("T");
        when(execution.getProcessInstanceId()).thenReturn("instance");
        when(execution.getActivityId()).thenReturn("calculate");
        var instanceQuery = mock(ProcessInstanceQuery.class);
        when(runtime.createProcessInstanceQuery()).thenReturn(instanceQuery);
        when(instanceQuery.processInstanceId("instance")).thenReturn(instanceQuery);
        when(instanceQuery.singleResult()).thenReturn(engineInstance);
        when(engineInstance.getId()).thenReturn("instance");
        when(engineInstance.getTenantId()).thenReturn("T");
        when(engineInstance.getProcessDefinitionId()).thenReturn("deployed:1");
        when(engineInstance.getBusinessKey()).thenReturn("business");
        instance.setTenantId("T");
        instance.setProcessInstanceId("instance");
        instance.setProcessDefId("P");
        instance.setProcessVersionNo(1);
        instance.setFlowableProcessDefId("deployed:1");
        instance.setBusinessKey("business");
        instance.setDocumentNumber("DOC001");
        instance.setFormDataId("data");
        instance.setInstanceStatus("RUNNING");
        instance.setInitiatorAccount("initiator");
        when(instances.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(instance)));
        version.setTenantId("T");
        version.setProcessDefId("P");
        version.setVersionNo(1);
        version.setVersionStatus("RETIRED");
        version.setFlowableProcessDefId("deployed:1");
        when(versions.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(version)));
        form.setTenantId("T");
        form.setFormDataId("data");
        form.setFormId("F");
        form.setFormVersionNo(1);
        form.setBusinessKey("business");
        form.setDocumentNumber("DOC001");
        form.setDataStatus("SUBMITTED");
        form.setOwnerAccount("applicant");
        form.setOwnerOrgUnitId("ORG");
        form.setRevisionNo(3);
        form.setLockVersion(4);
        form.setDataContent(GroovyRuntimeFixture.FORM_DATA);
        when(forms.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(form)));
        var preparation = GroovyRuntimeFixture.preparation();
        when(definitions.load("T", "P", 1, "calculate", "F", 1, GroovyRuntimeFixture.PROFILE))
                .thenReturn(preparation);
        task.setId("calculate");
        task.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        task.setImplementation("${fmGroovyTaskDelegate}");
        task.setAsynchronous(true);
        task.setExclusive(true);
        task.setFailedJobRetryTimeCycleValue("R0/PT1M");
        addField("flowmintBindingId", preparation.bindingId());
        addField("flowmintBindingSha256", preparation.bindingSha256());
        var model = new BpmnModel();
        var process = new org.flowable.bpmn.model.Process();
        process.setId("P");
        process.addFlowElement(task);
        model.addProcess(process);
        when(repository.getBpmnModel("deployed:1")).thenReturn(model);
    }

    @Test
    void usesSavedFormAndDistinctPersistedApplicantAndInitiator() throws Exception {
        var loaded = load();
        assertEquals(GroovyRuntimeFixture.loaded().snapshot(), loaded.snapshot());
        assertEquals(4, loaded.expectedFormLock());
        assertEquals(3, loaded.snapshot().revisionNo());
        verify(forms).selectListByParams(Map.of("tenantId", "T", "formDataId", "data"));
        verify(definitions).load("T", "P", 1, "calculate", "F", 1, GroovyRuntimeFixture.PROFILE);
    }

    @Test
    void refusesForeignExecutionBeforeReadingBusinessData() {
        when(execution.getTenantId()).thenReturn("OTHER");
        assertThrows(ServiceException.class, this::load);
        verifyNoInteractions(instances, forms, versions, definitions, repository);
    }

    @Test
    void refusesEndedSuspendedOrMissingEngineInstance() {
        when(execution.isEnded()).thenReturn(true);
        assertThrows(ServiceException.class, this::load);
        when(execution.isEnded()).thenReturn(false);
        when(engineInstance.isSuspended()).thenReturn(true);
        assertThrows(ServiceException.class, this::load);
        when(runtime.createProcessInstanceQuery().singleResult()).thenReturn(null);
        assertThrows(ServiceException.class, this::load);
        verifyNoInteractions(instances, forms, versions, definitions);
    }

    @Test
    void refusesCancelledInstanceAndDeploymentOrBusinessKeySubstitution() {
        instance.setInstanceStatus("CANCELLED");
        assertThrows(ServiceException.class, this::load);
        instance.setInstanceStatus("RUNNING");
        instance.setBusinessKey("foreign-business");
        assertThrows(ServiceException.class, this::load);
        instance.setBusinessKey("business");
        version.setFlowableProcessDefId("deployed:99");
        assertThrows(ServiceException.class, this::load);
        verifyNoInteractions(forms, definitions);
    }

    @Test
    void refusesForeignAmbiguousOrUnversionedForm() throws Exception {
        form.setTenantId("OTHER");
        assertThrows(ServiceException.class, this::load);
        form.setTenantId("T");
        form.setLockVersion(null);
        assertThrows(ServiceException.class, this::load);
        form.setLockVersion(4);
        when(forms.selectListByParams(anyMap())).thenReturn(GroovyRuntimeFixture.result(List.of(form, form)));
        assertThrows(ServiceException.class, this::load);
        verifyNoInteractions(definitions);
    }

    @Test
    void refusesReboundNodeDuplicateFieldsAndArbitraryDelegate() {
        task.getFieldExtensions().getFirst().setStringValue("another-binding");
        assertThrows(ServiceException.class, this::load);
        task.getFieldExtensions().getFirst().setStringValue("calculation");
        addField("flowmintBindingId", "calculation");
        assertThrows(ServiceException.class, this::load);
        task.setImplementation("${otherDelegate}");
        assertThrows(ServiceException.class, this::load);
    }

    private IFmGroovyRuntimeInputLogicService.LoadedInput load() throws ServiceException {
        return logic.load("T", "execution", "invocation", GroovyRuntimeFixture.START, GroovyRuntimeFixture.PROFILE);
    }

    private void addField(String name, String value) {
        var field = new FieldExtension();
        field.setFieldName(name);
        field.setStringValue(value);
        task.getFieldExtensions().add(field);
    }
}
