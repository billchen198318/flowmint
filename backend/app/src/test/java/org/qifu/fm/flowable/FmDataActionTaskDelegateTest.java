package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.domain.runtime.FmFormSubmissionValidator;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmFormVersionService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

class FmDataActionTaskDelegateTest {

    @Test
    void executesWithoutFormWriteWhenNoResponseMappingIsConfigured() throws Exception {
        for (String responseMapping : new String[] {null, "", "{}"}) {
            IFmDataActionLogicService actions = mock(IFmDataActionLogicService.class);
            IFmFormDataService formDataService = mock(IFmFormDataService.class);
            IFmFormSnapshotService snapshots = mock(IFmFormSnapshotService.class);
            IFmFormVersionService versions = mock(IFmFormVersionService.class);
            DelegateExecution execution = mock(DelegateExecution.class);
            ServiceTask task = mock(ServiceTask.class);
            when(execution.getCurrentFlowElement()).thenReturn(task);
            when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_TENANT_ID)).thenReturn("T1");
            when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT))
                    .thenReturn("applicant");
            when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                    "actionCode")).thenReturn("DO_WORK");
            when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                    "actionVersion")).thenReturn("1");
            when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                    "responseMapping")).thenReturn(responseMapping);
            DefaultResult<FmDataActionExecutionView> result = new DefaultResult<>();
            result.setValue(new FmDataActionExecutionView("EX1", "DO_WORK", 1, false, Map.of()));
            when(actions.execute("T1", "DO_WORK", 1, Map.of(), "applicant")).thenReturn(result);

            new FmDataActionTaskDelegate(actions, formDataService, snapshots,
                    versions, new FmFormSubmissionValidator(new ObjectMapper()),
                    new ObjectMapper()).execute(execution);

            verify(actions).execute("T1", "DO_WORK", 1, Map.of(), "applicant");
            verifyNoInteractions(formDataService);
            verifyNoInteractions(snapshots);
            verify(execution, never()).getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID);
            verify(execution, never()).setVariable(eq(FmTaskAssignmentListener.VARIABLE_FORM_DATA), any());
        }
    }

    @Test
    void mapsRequestExecutesFixedVersionAndPersistsResponse() throws Exception {
        IFmDataActionLogicService actions = mock(IFmDataActionLogicService.class);
        IFmFormDataService formDataService = mock(IFmFormDataService.class);
        IFmFormSnapshotService snapshots = mock(IFmFormSnapshotService.class);
        IFmFormVersionService versions = mock(IFmFormVersionService.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        ServiceTask task = mock(ServiceTask.class);
        when(execution.getCurrentFlowElement()).thenReturn(task);
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_TENANT_ID))
                .thenReturn("T1");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT))
                .thenReturn("applicant");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID))
                .thenReturn("FD1");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA))
                .thenReturn(Map.of("employeeId", "A001"));
        when(execution.getProcessInstanceId()).thenReturn("PI1");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "actionCode")).thenReturn("FM_GET_EMPLOYEE");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "actionVersion")).thenReturn("2");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "requestMapping")).thenReturn(
                        "{\"id\":\"FORM_DATA.employeeId\"}");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "responseMapping")).thenReturn(
                        "{\"employee.name\":\"FORM_DATA.employeeName\"}");
        DefaultResult<FmDataActionExecutionView> actionResult = new DefaultResult<>();
        actionResult.setValue(new FmDataActionExecutionView(
                "EX1", "FM_GET_EMPLOYEE", 2, false,
                Map.of("employee", Map.of("name", "Alice"))));
        when(actions.execute(eq("T1"), eq("FM_GET_EMPLOYEE"), eq(2),
                any(), eq("applicant"))).thenReturn(actionResult);
        FmFormData formData = new FmFormData();
        formData.setRevisionNo(1);
        formData.setLockVersion(0);
        formData.setFormId("F1");
        formData.setFormVersionNo(1);
        FmFormVersion version = new FmFormVersion();
        version.setSchemaContent("{\"components\":[{\"type\":\"textfield\",\"key\":\"employeeId\"},{\"type\":\"textfield\",\"key\":\"employeeName\"}]}");
        DefaultResult<List<FmFormVersion>> versionResult = new DefaultResult<>();
        versionResult.setValue(List.of(version));
        when(versions.selectListByParams(any())).thenReturn(versionResult);
        FmFormData updatedForm = new FmFormData();
        updatedForm.setRevisionNo(2);
        updatedForm.setLockVersion(1);
        updatedForm.setFormVersionNo(1);
        DefaultResult<List<FmFormData>> formResult = new DefaultResult<>();
        formResult.setValue(List.of(formData));
        DefaultResult<List<FmFormData>> updatedResult = new DefaultResult<>();
        updatedResult.setValue(List.of(updatedForm));
        when(formDataService.selectListByParams(any(), eq("FORM_DATA_ID"), eq("ASC")))
                .thenReturn(formResult, updatedResult);
        when(formDataService.updateDataContent(eq("T1"), eq("FD1"), any(), eq(0)))
                .thenAnswer(invocation -> {
                    updatedForm.setDataContent(invocation.getArgument(2));
                    return 1;
                });

        when(snapshots.insertSystemTaskSnapshot(any())).thenReturn("SN1");
        new FmDataActionTaskDelegate(actions, formDataService, snapshots, versions,
                new FmFormSubmissionValidator(new ObjectMapper()), new ObjectMapper())
                .execute(execution);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> request = ArgumentCaptor.forClass(Map.class);
        verify(actions).execute(eq("T1"), eq("FM_GET_EMPLOYEE"), eq(2),
                request.capture(), eq("applicant"));
        assertEquals("A001", request.getValue().get("id"));
        ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
        verify(formDataService).updateDataContent(eq("T1"), eq("FD1"),
                content.capture(), eq(0));
        Map<String, Object> saved = new ObjectMapper().readValue(
                content.getValue(), new TypeReference<Map<String, Object>>() { });
        assertEquals("A001", saved.get("employeeId"));
        assertEquals("Alice", saved.get("employeeName"));
        ArgumentCaptor<FmFormSnapshot> snapshot = ArgumentCaptor.forClass(FmFormSnapshot.class);
        verify(snapshots).insertSystemTaskSnapshot(snapshot.capture());
        assertEquals("PI1", snapshot.getValue().getProcessInstanceId());
        assertEquals(2, snapshot.getValue().getRevisionNo());
        assertEquals(content.getValue(), snapshot.getValue().getDataContent());

        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "responseMapping")).thenReturn(
                        "{\"employee.name\":\"FORM_DATA.undeclaredField\"}");
        when(formDataService.selectListByParams(any(), eq("FORM_DATA_ID"), eq("ASC")))
                .thenReturn(formResult);
        assertThrows(IllegalStateException.class,
                () -> new FmDataActionTaskDelegate(actions, formDataService, snapshots,
                        versions, new FmFormSubmissionValidator(new ObjectMapper()),
                        new ObjectMapper()).execute(execution));
        verify(formDataService, times(1)).updateDataContent(eq("T1"), eq("FD1"), any(), eq(0));
    }
}
