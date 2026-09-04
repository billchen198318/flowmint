package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmDataActionTaskPublishValidator;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.logic.IFmDataActionLogicService;
import org.qifu.fm.service.IFmFormDataService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

class FmDataActionTaskDelegateTest {

    @Test
    void mapsRequestExecutesFixedVersionAndPersistsResponse() throws Exception {
        IFmDataActionLogicService actions = mock(IFmDataActionLogicService.class);
        IFmFormDataService formDataService = mock(IFmFormDataService.class);
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
        DefaultResult<List<FmFormData>> formResult = new DefaultResult<>();
        formResult.setValue(List.of(formData));
        when(formDataService.selectListByParams(any(), eq("FORM_DATA_ID"), eq("ASC")))
                .thenReturn(formResult);
        when(formDataService.updateDataContent(eq("T1"), eq("FD1"), any(), eq(0)))
                .thenReturn(1);

        new FmDataActionTaskDelegate(actions, formDataService, new ObjectMapper())
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
    }
}
