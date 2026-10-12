package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.view.FmProcessSubmitView;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

class FmProcessRuntimeIdempotencyTest {

    @Test
    void replayRequiresSameProcessDefinition() throws Exception {
        IFmFormDataService forms = mock(IFmFormDataService.class);
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        IFmTenantService tenants = mock(IFmTenantService.class);
        FmTenant tenant = new FmTenant();
        tenant.setTenantId("TENANT");
        tenant.setStatus("ACTIVE");
        when(tenants.selectListByParams(anyMap())).thenReturn(result(List.of(tenant)));
        FmFormData form = new FmFormData();
        form.setFormId("SHARED_FORM");
        form.setFormVersionNo(1);
        form.setOwnerAccount("applicant");
        form.setFormDataId("FORM_DATA");
        form.setBusinessKey("BUSINESS_KEY");
        form.setDataContent("{\"subject\":\"same\"}");
        FmProcessInstance process = new FmProcessInstance();
        process.setProcessDefId("PROCESS_A");
        process.setProcessVersionNo(1);
        process.setInitiatorAccount("starter");
        process.setFormDataId("FORM_DATA");
        process.setProcessInstanceId("INSTANCE_A");
        when(forms.selectListByParams(anyMap())).thenReturn(result(List.of(form)));
        when(processes.selectListByParams(anyMap())).thenReturn(result(List.of(process)));
        FmProcessRuntimeLogicServiceImpl logic = new FmProcessRuntimeLogicServiceImpl(
                null, null, null, null, null, null, null, null, forms, processes,
                null, null, null, null, null, null, null, null, tenants, null,
                null, null, new ObjectMapper(), null, null);
        FmProcessSubmitCommand otherProcess = new FmProcessSubmitCommand(
                "TENANT", "PROCESS_B", "SHARED_FORM", 1, "SAME_KEY",
                "applicant", Map.of("subject", "same"), null, 1);

        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                logic, "existingSubmission", otherProcess, "starter"));
        FmProcessSubmitCommand sameProcess = new FmProcessSubmitCommand(
                "TENANT", "PROCESS_A", "SHARED_FORM", 1, "SAME_KEY",
                "applicant", Map.of("subject", "same"), null, 1);
        FmProcessSubmitView replay = ReflectionTestUtils.invokeMethod(
                logic, "existingSubmission", sameProcess, "starter");
        assertEquals("INSTANCE_A", replay.processInstanceId());
        assertEquals("INSTANCE_A", logic.submitAs(sameProcess, "starter")
                .getValue().processInstanceId());

        FmProcessSubmitCommand changedVersion = new FmProcessSubmitCommand(
                "TENANT", "PROCESS_A", "SHARED_FORM", 1, "SAME_KEY",
                "applicant", Map.of("subject", "same"), null, 2);
        assertThrows(ServiceException.class, () -> logic.submitAs(changedVersion, "starter"));
        tenant.setStatus("INACTIVE");
        assertThrows(ServiceException.class, () -> logic.submitAs(sameProcess, "starter"));
    }

    @Test
    void rejectsProcessVersionChangedAfterStartPageLoaded() {
        FmProcessRuntimeLogicServiceImpl logic = new FmProcessRuntimeLogicServiceImpl(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
        FmProcessVersion published = new FmProcessVersion();
        published.setVersionNo(2);
        FmProcessSubmitCommand loadedVersion = new FmProcessSubmitCommand(
                "TENANT", "PROCESS_A", "SHARED_FORM", 1, "KEY",
                "applicant", Map.of("subject", "same"), null, 1);

        assertThrows(ServiceException.class, () -> ReflectionTestUtils.invokeMethod(
                logic, "requireExpectedProcessVersion", loadedVersion, published));
        FmProcessSubmitCommand currentVersion = new FmProcessSubmitCommand(
                "TENANT", "PROCESS_A", "SHARED_FORM", 1, "KEY",
                "applicant", Map.of("subject", "same"), null, 2);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(
                        logic, "requireExpectedProcessVersion", currentVersion, published));
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
