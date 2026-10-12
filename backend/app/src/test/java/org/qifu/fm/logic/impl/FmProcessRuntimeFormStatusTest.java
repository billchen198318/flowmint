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
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.springframework.test.util.ReflectionTestUtils;

class FmProcessRuntimeFormStatusTest {

    @Test
    void draftKeepsPublishedVersionStartableAndInactiveDoesNot() throws Exception {
        IFmFormDefService forms = mock(IFmFormDefService.class);
        FmProcessRuntimeLogicServiceImpl logic = new FmProcessRuntimeLogicServiceImpl(
                null, null, null, null, forms, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
        FmFormDef draft = form("DRAFT");
        when(forms.selectListByParams(anyMap())).thenAnswer(call -> {
            Map<?, ?> parameters = call.getArgument(0);
            return result(parameters.containsKey("status") ? List.of() : List.of(draft));
        });

        FmFormDef found = ReflectionTestUtils.invokeMethod(
                logic, "activeFormDef", "tenant", "form");
        assertEquals("form", found.getFormId());

        when(forms.selectListByParams(anyMap())).thenReturn(result(List.of(form("INACTIVE"))));
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        logic, "activeFormDef", "tenant", "form"));
    }

    @Test
    void retiredFormVersionRemainsAvailableToBoundPublishedProcess() throws Exception {
        IFmFormVersionService versions = mock(IFmFormVersionService.class);
        FmProcessRuntimeLogicServiceImpl logic = new FmProcessRuntimeLogicServiceImpl(
                null, null, null, versions, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
        FmFormVersion retired = new FmFormVersion();
        retired.setVersionStatus("RETIRED");
        when(versions.selectListByParams(anyMap())).thenAnswer(call -> {
            Map<?, ?> parameters = call.getArgument(0);
            return result(parameters.containsKey("versionStatus")
                    ? List.of() : List.of(retired));
        });

        FmFormVersion found = ReflectionTestUtils.invokeMethod(
                logic, "publishedFormVersion", "tenant", "form", 1);
        assertEquals("RETIRED", found.getVersionStatus());

        retired.setVersionStatus("DRAFT");
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        logic, "publishedFormVersion", "tenant", "form", 1));
    }

    private static FmFormDef form(String status) {
        FmFormDef form = new FmFormDef();
        form.setFormId("form");
        form.setStatus(status);
        return form;
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
