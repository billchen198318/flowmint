package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ServiceTask;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.entity.FmDataActionVersion;
import org.qifu.fm.service.IFmDataActionService;
import org.qifu.fm.service.IFmDataActionStepService;
import org.qifu.fm.service.IFmDataActionVersionService;

import tools.jackson.databind.ObjectMapper;

class FmDataActionTaskPublishValidatorTest {
    @Test
    void sortsByExistingColumnAndRejectsInactiveResponseSource() throws Exception {
        IFmDataActionService actions = mock(IFmDataActionService.class);
        IFmDataActionVersionService versions = mock(IFmDataActionVersionService.class);
        IFmDataActionStepService steps = mock(IFmDataActionStepService.class);
        FmDataAction action = new FmDataAction();
        action.setTenantId("T");
        action.setActionId("A");
        action.setActionCode("CODE");
        action.setActionType("QUERY");
        action.setRequestSchema("{}");
        FmDataActionVersion version = new FmDataActionVersion();
        version.setVersionNo(1);
        version.setVersionStatus("PUBLISHED");
        FmDataActionStep step = new FmDataActionStep();
        step.setResultKey("secret");
        step.setResultMode("OBJECT");
        step.setStatus("INACTIVE");
        when(actions.selectListByParams(any(), eq("ACTION_CODE"), eq("ASC")))
                .thenReturn(result(List.of(action)));
        when(versions.selectListByParams(any(), eq("VERSION_NO"), eq("DESC")))
                .thenReturn(result(List.of(version)));
        when(steps.selectListByParams(any(), eq("EXECUTION_ORDER"), eq("ASC")))
                .thenReturn(result(List.of(step)));
        ServiceTask task = mock(ServiceTask.class);
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "taskType")).thenReturn("DATA_ACTION");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "actionCode")).thenReturn("CODE");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "actionVersion")).thenReturn("1");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "requestMapping")).thenReturn("{}");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "responseMapping")).thenReturn("{\"secret\":\"FORM_DATA.secret\"}");
        org.flowable.bpmn.model.Process process = mock(org.flowable.bpmn.model.Process.class);
        when(process.findFlowElementsOfType(ServiceTask.class)).thenReturn(List.of(task));
        BpmnModel model = mock(BpmnModel.class);
        when(model.getMainProcess()).thenReturn(process);
        FmDataActionTaskPublishValidator validator = new FmDataActionTaskPublishValidator(
                actions, versions, steps, new ObjectMapper());

        assertThrows(ServiceException.class, () -> validator.validate("T", model));
        verify(steps).selectListByParams(any(), eq("EXECUTION_ORDER"), eq("ASC"));

        step.setStatus("ACTIVE");
        assertDoesNotThrow(() -> validator.validate("T", model));
    }

    @Test
    void responseTargetMustExistInEveryBoundFormSchema() throws Exception {
        ServiceTask task = mock(ServiceTask.class);
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "taskType")).thenReturn("DATA_ACTION");
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "responseMapping")).thenReturn("{\"result.name\":\"FORM_DATA.details.name\"}");
        org.flowable.bpmn.model.Process process = mock(org.flowable.bpmn.model.Process.class);
        when(process.findFlowElementsOfType(ServiceTask.class)).thenReturn(List.of(task));
        BpmnModel model = mock(BpmnModel.class);
        when(model.getMainProcess()).thenReturn(process);
        FmDataActionTaskPublishValidator validator = new FmDataActionTaskPublishValidator(
                mock(IFmDataActionService.class), mock(IFmDataActionVersionService.class),
                mock(IFmDataActionStepService.class), new ObjectMapper());
        String valid = "{\"components\":[{\"type\":\"container\",\"key\":\"details\","
                + "\"components\":[{\"type\":\"textfield\",\"key\":\"name\"}]}]}";
        String missing = "{\"components\":[{\"type\":\"textfield\",\"key\":\"other\"}]}";
        assertDoesNotThrow(() -> validator.validateFormTargets(model, List.of(valid)));
        assertThrows(ServiceException.class,
                () -> validator.validateFormTargets(model, List.of(valid, missing)));
        when(task.getAttributeValue(FmDataActionTaskPublishValidator.FLOWMINT_NAMESPACE,
                "responseMapping")).thenReturn("{\"result.name\":\"FORM_DATA.unknown\"}");
        assertThrows(ServiceException.class,
                () -> validator.validateFormTargets(model, List.of(valid)));
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
