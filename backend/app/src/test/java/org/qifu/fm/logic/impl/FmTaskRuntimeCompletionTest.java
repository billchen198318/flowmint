package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmTaskActionRequest;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.test.util.ReflectionTestUtils;

class FmTaskRuntimeCompletionTest {

    @Test
    void currentActorEligibilityRequiresActiveEmployee() throws Exception {
        FmTaskRuntimeLogicServiceImpl logic = fixture(null).logic();
        IFmEmployeeService employees = (IFmEmployeeService) ReflectionTestUtils.getField(
                logic, "employeeService");
        when(employees.selectListByParams(any())).thenReturn(result(List.of()));

        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                logic, "activeEmployee", "tenant-1", "actor", new Date()));

        verify(employees).selectListByParams(argThat(parameters ->
                "tenant-1".equals(parameters.get("tenantId"))
                        && "actor".equals(parameters.get("account"))
                        && "ACTIVE".equals(parameters.get("status"))));
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }

    @Test
    void rejectAndReturnCannotUpdateFormData() throws Exception {
        FmTaskRuntimeLogicServiceImpl logic = fixture(null).logic();
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setAssignmentMode("ASSIGNEE");
        policy.setAllowReject("Y");
        policy.setAllowReturn("Y");
        for (String action : new String[] {"REJECT", "RETURN"}) {
            FmTaskActionRequest request = new FmTaskActionRequest(
                    "task-1", action, "comment", "reason", null, 1,
                    Map.of("subject", "altered"));
            assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                    logic, "validatePolicy", request, policy, "process-1", "review"));
        }
    }

    @Test
    void marksProcessAndFormCompletedWhenResubmitEndsFlowableInstance() throws Exception {
        Fixture fixture = fixture(null);

        String status = fixture.logic.completeTask(fixture.task, fixture.process,
                fixture.formData, "applicant", fixture.now);

        assertEquals("COMPLETED", status);
        assertEquals("COMPLETED", fixture.formData.getDataStatus());
        verify(fixture.processService).updateStatus(
                "tenant-1", "process-1", "RUNNING", "COMPLETED",
                fixture.now, "applicant");
        verify(fixture.formDataService).update(fixture.formData);
    }

    @Test
    void acceptsCompletionAlreadyRecordedByFlowableListener() throws Exception {
        Fixture fixture = fixture(null);
        when(fixture.processService.updateStatus(
                eq("tenant-1"), eq("process-1"), eq("RUNNING"), eq("COMPLETED"),
                any(Date.class), eq("applicant"))).thenReturn(false);
        FmProcessInstance completed = new FmProcessInstance();
        completed.setInstanceStatus("COMPLETED");
        when(fixture.processService.lockInstance("tenant-1", "process-1"))
                .thenReturn(completed);

        String status = fixture.logic.completeTask(fixture.task, fixture.process,
                fixture.formData, "applicant", fixture.now);

        assertEquals("COMPLETED", status);
        assertEquals("COMPLETED", fixture.formData.getDataStatus());
        verify(fixture.formDataService).update(fixture.formData);
    }

    @Test
    void keepsIndexesRunningWhenResubmitContinuesFlowableInstance() throws Exception {
        Fixture fixture = fixture(mock(ProcessInstance.class));

        String status = fixture.logic.completeTask(fixture.task, fixture.process,
                fixture.formData, "applicant", fixture.now);

        assertEquals("RUNNING", status);
        assertEquals("RUNNING", fixture.process.getInstanceStatus());
        assertEquals("SUBMITTED", fixture.formData.getDataStatus());
    }

    private Fixture fixture(ProcessInstance activeInstance) throws Exception {
        TaskService taskService = mock(TaskService.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        IFmProcessInstanceService processService = mock(IFmProcessInstanceService.class);
        IFmFormDataService formDataService = mock(IFmFormDataService.class);
        ProcessInstanceQuery query = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(query);
        when(query.processInstanceId("process-1")).thenReturn(query);
        when(query.singleResult()).thenReturn(activeInstance);
        when(processService.updateStatus(
                eq("tenant-1"), eq("process-1"), eq("RUNNING"), eq("COMPLETED"),
                any(Date.class), eq("applicant"))).thenReturn(true);

        Constructor<?> constructor = FmTaskRuntimeLogicServiceImpl.class
                .getConstructors()[0];
        Object[] arguments = new Object[constructor.getParameterCount()];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> type = parameterTypes[index];
            if (type == TaskService.class) {
                arguments[index] = taskService;
            } else if (type == RuntimeService.class) {
                arguments[index] = runtimeService;
            } else if (type == IFmProcessInstanceService.class) {
                arguments[index] = processService;
            } else if (type == IFmFormDataService.class) {
                arguments[index] = formDataService;
            } else {
                arguments[index] = mock(type);
            }
        }
        FmTaskRuntimeLogicServiceImpl logic =
                (FmTaskRuntimeLogicServiceImpl) constructor.newInstance(arguments);

        Task task = mock(Task.class);
        when(task.getId()).thenReturn("task-1");
        FmProcessInstance process = new FmProcessInstance();
        process.setTenantId("tenant-1");
        process.setProcessInstanceId("process-1");
        process.setInstanceStatus("RUNNING");
        FmFormData formData = new FmFormData();
        formData.setDataStatus("SUBMITTED");
        Date now = new Date();
        return new Fixture(logic, task, process, formData, now,
                processService, formDataService);
    }

    private record Fixture(
            FmTaskRuntimeLogicServiceImpl logic,
            Task task,
            FmProcessInstance process,
            FmFormData formData,
            Date now,
            IFmProcessInstanceService processService,
            IFmFormDataService formDataService) {
    }
}
