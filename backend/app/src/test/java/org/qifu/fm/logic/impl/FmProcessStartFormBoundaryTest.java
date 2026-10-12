package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyMap;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.dto.command.FmProcessSubmitCommand;
import org.qifu.fm.dto.view.FmProcessStartFormView;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.qifu.fm.service.IFmFormDefService;
import org.qifu.fm.service.IFmFormVersionService;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

class FmProcessStartFormBoundaryTest {

    @Test
    void multipleTaskFormsCannotBePresentedOrSubmittedAsStartForms() throws Exception {
        FmProcessRuntimeLogicServiceImpl logic =
                mock(FmProcessRuntimeLogicServiceImpl.class, CALLS_REAL_METHODS);
        IFmTaskFormRuleService rules = mock(IFmTaskFormRuleService.class);
        ReflectionTestUtils.setField(logic, "taskFormRuleService", rules);
        ReflectionTestUtils.setField(logic, "objectMapper", new ObjectMapper());
        FmProcessVersion version = new FmProcessVersion();
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        FmTaskFormRule application = rule("application", "F1");
        FmTaskFormRule approval = rule("approval", "F2");
        when(rules.findByVersion("T1", "P1", 1))
                .thenReturn(List.of(application, approval));
        FmProcessSubmitCommand command = new FmProcessSubmitCommand(
                "T1", "P1", "F2", 1, "key", "applicant", Map.of(), null, 1);

        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(logic, "startForms", "T1", version));
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(logic, "ensureFormBound", command, version));

        IFmFormVersionService formVersions = mock(IFmFormVersionService.class);
        IFmFormDefService formDefs = mock(IFmFormDefService.class);
        ReflectionTestUtils.setField(logic, "formVersionService", formVersions);
        ReflectionTestUtils.setField(logic, "formDefService", formDefs);
        FmFormVersion formVersion = new FmFormVersion();
        formVersion.setVersionStatus("PUBLISHED");
        DefaultResult<List<FmFormVersion>> versionRows = new DefaultResult<>();
        versionRows.setValue(List.of(formVersion));
        when(formVersions.selectListByParams(anyMap())).thenReturn(versionRows);
        FmFormDef formDef = new FmFormDef();
        formDef.setStatus("PUBLISHED");
        DefaultResult<List<FmFormDef>> defRows = new DefaultResult<>();
        defRows.setValue(List.of(formDef));
        when(formDefs.selectListByParams(anyMap())).thenReturn(defRows);

        application.setFieldPolicy("{\"default\":\"READ\",\"fields\":{},\"startForm\":true}");
        List<FmProcessStartFormView> visible = ReflectionTestUtils.invokeMethod(
                logic, "startForms", "T1", version);
        org.junit.jupiter.api.Assertions.assertEquals(1, visible.size());
        org.junit.jupiter.api.Assertions.assertEquals("F1", visible.getFirst().formId());
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(logic,
                "ensureFormBound", new FmProcessSubmitCommand("T1", "P1", "F1", 1,
                        "key", "applicant", Map.of(), null, 1), version));
        assertThrows(ServiceException.class,
                () -> ReflectionTestUtils.invokeMethod(logic, "ensureFormBound", command, version));

        approval.setFieldPolicy("{\"default\":\"READ\",\"fields\":{},\"startForm\":true}");
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(logic,
                "ensureFormBound", command, version));

        when(rules.findByVersion("T1", "P1", 1))
                .thenReturn(List.of(application, rule("approval", "F1")));
        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(logic, "ensureFormBound",
                        new FmProcessSubmitCommand("T1", "P1", "F1", 1, "key",
                                "applicant", Map.of(), null, 1), version));
    }

    private FmTaskFormRule rule(String taskKey, String formId) {
        FmTaskFormRule rule = new FmTaskFormRule();
        rule.setTaskDefKey(taskKey);
        rule.setFormId(formId);
        rule.setFormVersionNo(1);
        rule.setFieldPolicy("{\"default\":\"READ\",\"fields\":{}}");
        return rule;
    }
}
