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

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.workflow.FmGroovyDraftValidator;
import org.qifu.fm.domain.workflow.FmGroovyVersionManifest;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessGroovyManifest;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.logic.impl.FmGroovyRuntimeDefinitionLogicServiceImpl;
import org.qifu.fm.service.IFmFormVersionService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessGroovyManifestService;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskFormRuleService;

class FmGroovyRuntimeDefinitionLogicTest {

    private final IFmProcessDefService definitions = mock(IFmProcessDefService.class);
    private final IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
    private final IFmProcessGroovyManifestService manifests = mock(IFmProcessGroovyManifestService.class);
    private final IFmProcessSystemTaskService bindings = mock(IFmProcessSystemTaskService.class);
    private final IFmTaskFormRuleService rules = mock(IFmTaskFormRuleService.class);
    private final IFmFormVersionService forms = mock(IFmFormVersionService.class);
    private final FmGroovyRuntimeDefinitionLogicServiceImpl logic = new FmGroovyRuntimeDefinitionLogicServiceImpl(
            definitions, versions, manifests, bindings, rules, forms);
    private final FmGroovyVersionManifest.EngineProfile profile = new FmGroovyVersionManifest.EngineProfile(
            "5.0.6", "ExampleJDK", "21.0.10+7", "local/worker@sha256:" + "a".repeat(64), "1");

    @Test
    void loadsRetiredFixedVersionsWithoutUsingCurrentVersion() throws Exception {
        var manifest = fixture();
        var prepared = logic.load("T", "P", 1, "calculate", "F", 1, profile);
        assertEquals(manifest.sha256(), prepared.manifestSha256());
        verify(versions).selectListByParams(Map.of("tenantId", "T", "processDefId", "P", "versionNo", 1));
        verify(forms).selectListByParams(Map.of("tenantId", "T", "formId", "F", "versionNo", 1));
    }

    @Test
    void refusesMissingVersionBeforeReadingNewTables() throws Exception {
        fixture();
        when(versions.selectListByParams(anyMap())).thenReturn(result(List.of()));
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
        verifyNoInteractions(manifests, forms, bindings, rules);
    }

    @Test
    void refusesDifferentFormBindingAndForeignTenantRows() throws Exception {
        fixture();
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "OTHER", 1, profile));
        verifyNoInteractions(forms, bindings);
        var foreign = new FmProcessDef();
        foreign.setTenantId("OTHER");
        foreign.setProcessDefId("P");
        when(definitions.selectListByParams(anyMap())).thenReturn(result(List.of(foreign)));
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
    }

    @Test
    void refusesInvalidIdentityWithoutQueryingServices() {
        assertThrows(ServiceException.class, () -> logic.load(null, "P", 1, "calculate", "F", 1, profile));
        verifyNoInteractions(definitions, versions, manifests, forms, bindings, rules);
    }

    @Test
    void refusesAmbiguousOrDraftVersion() throws Exception {
        fixture();
        var version = versions.selectListByParams(Map.of()).getValue().getFirst();
        when(versions.selectListByParams(anyMap())).thenReturn(result(List.of(version, version)));
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
        when(versions.selectListByParams(anyMap())).thenReturn(result(List.of(version)));
        version.setVersionStatus("DRAFT");
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
        verifyNoInteractions(manifests, forms, bindings, rules);
    }

    @Test
    void refusesProfileSubstitutionAndTamperedManifest() throws Exception {
        fixture();
        var stored = manifests.selectListByParams(Map.of()).getValue().getFirst();
        stored.setEngineProfile("{}");
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
        stored.setEngineProfile(profile.canonical());
        stored.setManifestSha256("b".repeat(64));
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
    }

    @Test
    void refusesDraftFormAndForeignBindingEvenWhenServiceReturnsThem() throws Exception {
        fixture();
        var form = forms.selectListByParams(Map.of()).getValue().getFirst();
        form.setVersionStatus("DRAFT");
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
        form.setVersionStatus("RETIRED");
        bindings.findVersion("T", "P", 1).getFirst().setTenantId("OTHER");
        assertThrows(ServiceException.class, () -> logic.load("T", "P", 1, "calculate", "F", 1, profile));
    }

    private FmGroovyVersionManifest.Manifest fixture() throws Exception {
        var definition = new FmProcessDef();
        definition.setTenantId("T");
        definition.setProcessDefId("P");
        definition.setProcessKey("P");
        definition.setCurrentVersionNo(99);
        when(definitions.selectListByParams(anyMap())).thenReturn(result(List.of(definition)));
        var version = new FmProcessVersion();
        version.setTenantId("T");
        version.setProcessDefId("P");
        version.setVersionNo(1);
        version.setVersionStatus("RETIRED");
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
        var commands = List.of(new FmGroovyBindingCommand("calculate", "calculation", "return [:]",
                "{\"type\":\"object\",\"properties\":{}}", "{\"type\":\"object\",\"properties\":{}}",
                "{\"mappingVersion\":1,\"input\":{},\"output\":{}}", 3000));
        var manifest = FmGroovyVersionManifest.build(version, "P", commands, profile);
        version.setBpmnSha256(manifest.bpmnSha256());
        when(versions.selectListByParams(anyMap())).thenReturn(result(List.of(version)));
        var stored = new FmProcessGroovyManifest();
        stored.setTenantId("T");
        stored.setProcessDefId("P");
        stored.setVersionNo(1);
        stored.setBpmnSha256(manifest.bpmnSha256());
        stored.setEngineProfile(profile.canonical());
        stored.setManifestContent(manifest.canonicalJson());
        stored.setManifestSha256(manifest.sha256());
        when(manifests.selectListByParams(anyMap())).thenReturn(result(List.of(stored)));
        var rule = new FmTaskFormRule();
        rule.setTenantId("T");
        rule.setProcessDefId("P");
        rule.setProcessVersionNo(1);
        rule.setFormId("F");
        rule.setFormVersionNo(1);
        when(rules.findByVersion("T", "P", 1)).thenReturn(List.of(rule));
        var form = new FmFormVersion();
        form.setTenantId("T");
        form.setFormId("F");
        form.setVersionNo(1);
        form.setVersionStatus("RETIRED");
        form.setSchemaContent("{\"components\":[]}");
        when(forms.selectListByParams(anyMap())).thenReturn(result(List.of(form)));
        when(bindings.findVersion("T", "P", 1)).thenReturn(
                new FmGroovyDraftValidator().validate(version, version.getBpmnXml(), commands));
        return manifest;
    }

    private static <T> DefaultResult<List<T>> result(List<T> values) {
        var result = new DefaultResult<List<T>>();
        result.setValue(values);
        return result;
    }
}
