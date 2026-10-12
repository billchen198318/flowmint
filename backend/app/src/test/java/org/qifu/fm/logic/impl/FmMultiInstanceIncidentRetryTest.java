package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.dto.command.FmIncidentReassignRequest;
import org.qifu.fm.dto.command.FmIncidentRetryRequest;
import org.qifu.fm.dto.view.FmAssignmentIncidentView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.test.util.ReflectionTestUtils;

class FmMultiInstanceIncidentRetryTest {

    @Test
    void retryReturnsToResolverAndReassignCannotCollapseMultiApproval() throws Exception {
        FmIncidentOperationsLogicServiceImpl logic =
                mock(FmIncidentOperationsLogicServiceImpl.class, CALLS_REAL_METHODS);
        FmAssignmentIncidentRecorder recorder = mock(FmAssignmentIncidentRecorder.class);
        TaskService tasks = mock(TaskService.class);
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        IFmFormDataService forms = mock(IFmFormDataService.class);
        ReflectionTestUtils.setField(logic, "incidentRecorder", recorder);
        ReflectionTestUtils.setField(logic, "taskService", tasks);
        ReflectionTestUtils.setField(logic, "processInstanceService", processes);
        ReflectionTestUtils.setField(logic, "formDataService", forms);
        ReflectionTestUtils.setField(logic, "auditLogicService",
                mock(IFmRuntimeAuditLogicService.class));
        FmAssignmentIncidentView incident = new FmAssignmentIncidentView(
                "INC1", "PI1", "TASK1", "approval", "ASSIGNMENT", "RESOLVER_FAILED",
                "No candidates", "{}", "OPEN", null, null, null, null);
        when(recorder.requiredOpen("T1", "INC1")).thenReturn(incident);
        when(recorder.resolve(any(), any(), any(), any(), any())).thenReturn(true);
        TaskQuery query = mock(TaskQuery.class);
        Task task = mock(Task.class);
        when(tasks.createTaskQuery()).thenReturn(query);
        when(query.taskId("TASK1")).thenReturn(query);
        when(query.singleResult()).thenReturn(task);
        when(task.getTenantId()).thenReturn("T1");
        when(task.getCategory()).thenReturn("FLOWMINT_INCIDENT:INC1");
        when(task.getTaskDefinitionKey()).thenReturn("approval__fmMiIncident");
        when(task.getId()).thenReturn("TASK1");
        FmProcessInstance process = new FmProcessInstance();
        process.setInstanceStatus("RUNNING");
        process.setProcessInstanceId("PI1");
        process.setFormDataId("FD1");
        when(processes.selectListByParams(anyMap())).thenReturn(result(List.of(process)));
        FmFormData form = new FmFormData();
        form.setFormDataId("FD1");
        when(forms.selectListByParams(anyMap())).thenReturn(result(List.of(form)));
        User user = mock(User.class);
        when(user.getUserId()).thenReturn("operator");
        try (var users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            users.when(UserUtils::getCurrentUser).thenReturn(user);
            logic.retry("T1", new FmIncidentRetryRequest("INC1", "fixed rules"));
            verify(tasks).complete("TASK1");
            assertThrows(ServiceException.class, () -> logic.reassign("T1",
                    new FmIncidentReassignRequest("INC1", "someone", "override")));
        }
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
