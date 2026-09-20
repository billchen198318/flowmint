package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.domain.workflow.FmGroovyDesignAccess;
import org.qifu.fm.dto.command.FmProcessVersionCommand;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.impl.FmProcessDefLogicServiceImpl;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessSystemTaskService;
import org.qifu.fm.service.IFmProcessVersionService;

class FmProcessGroovyDraftLogicTest {

    private final IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
    private final IFmProcessDefService definitions = mock(IFmProcessDefService.class);
    private final IFmProcessSystemTaskService bindings = mock(IFmProcessSystemTaskService.class);
    private final FmTenantAccessGuard access = mock(FmTenantAccessGuard.class);
    private final FmGroovyDesignAccess groovyAccess = mock(FmGroovyDesignAccess.class);

    @Test
    void removingLastGroovyNodeStillRequiresDesignCapability() throws Exception {
        FmProcessVersion version = new FmProcessVersion();
        version.setOid("V1");
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionNo(1);
        version.setVersionStatus("DRAFT");
        when(versions.selectByPrimaryKey("V1")).thenReturn(result(version));
        when(versions.lockDraft("T1", "V1")).thenReturn(0);
        when(versions.advanceDraftLock("T1", "V1", 0)).thenReturn(1);
        var oldBinding = new org.qifu.fm.entity.FmProcessSystemTask();
        when(bindings.findVersion("T1", "P1", 1)).thenReturn(List.of(oldBinding));
        var definition = new FmProcessDef();
        definition.setProcessKey("P1");
        when(definitions.selectListByParams(any())).thenReturn(result(List.of(definition)));
        doThrow(new ServiceException("GROOVY_DESIGN_DENIED")).when(groovyAccess).require("T1");
        var command = new FmProcessVersionCommand("V1", """
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" targetNamespace="test">
                  <process id="P1"><startEvent id="start"/><endEvent id="end"/></process>
                </definitions>
                """, List.of(), List.of(), List.of(), List.of(), List.of(), 0);
        assertThrows(ServiceException.class, () -> logic().saveDraft(command));
        verify(groovyAccess).require("T1");
        verify(versions, never()).update(any());
        verify(bindings, never()).replaceDraftVersion(any(), any(), any(), any());
    }

    @Test
    void cloningGroovyVersionRequiresCapabilityBeforeInsertingVersion() throws Exception {
        var definition = new FmProcessDef();
        definition.setTenantId("T1");
        definition.setProcessDefId("P1");
        when(definitions.selectByPrimaryKey("D1")).thenReturn(result(definition));
        var source = new FmProcessVersion();
        source.setTenantId("T1");
        source.setProcessDefId("P1");
        source.setVersionNo(1);
        source.setVersionStatus("PUBLISHED");
        when(versions.selectListByParams(any(), any(), any())).thenReturn(result(List.of(source)));
        when(bindings.findVersion("T1", "P1", 1)).thenReturn(List.of(new org.qifu.fm.entity.FmProcessSystemTask()));
        doThrow(new ServiceException("GROOVY_DESIGN_DENIED")).when(groovyAccess).require("T1");
        assertThrows(ServiceException.class, () -> logic().createVersion("D1"));
        verify(groovyAccess).require("T1");
        verify(versions, never()).insert(any());
    }

    @Test
    void staleSaveCannotReplaceBindingsOrUpdateXml() throws Exception {
        FmProcessVersion version = new FmProcessVersion();
        version.setOid("V1");
        version.setTenantId("T1");
        version.setProcessDefId("P1");
        version.setVersionStatus("DRAFT");
        when(versions.selectByPrimaryKey("V1")).thenReturn(result(version));
        when(versions.lockDraft("T1", "V1")).thenReturn(3);
        when(versions.advanceDraftLock("T1", "V1", 2)).thenReturn(0);
        FmProcessDef definition = new FmProcessDef();
        definition.setProcessKey("P1");
        when(definitions.selectListByParams(any())).thenReturn(result(List.of(definition)));
        FmProcessVersionCommand command = new FmProcessVersionCommand("V1", """
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" targetNamespace="test">
                    <process id="P1"><startEvent id="start" /><endEvent id="end" /></process>
                </definitions>
                """, List.of(), List.of(), List.of(), List.of(), List.of(), 2);
        assertThrows(ServiceException.class, () -> logic().saveDraft(command));
        verify(access).requireAccess("T1");
        verifyNoInteractions(bindings);
        verify(versions, never()).update(any());
    }

    @Test
    void versionPublishedDuringWaitCannotBeEdited() throws Exception {
        FmProcessVersion stale = new FmProcessVersion();
        stale.setTenantId("T1");
        stale.setVersionStatus("DRAFT");
        when(versions.selectByPrimaryKey("V1")).thenReturn(result(stale));
        when(versions.lockDraft("T1", "V1")).thenReturn(null);
        assertThrows(ServiceException.class, () -> logic().saveDraft(new FmProcessVersionCommand(
                "V1", "unused", List.of(), List.of(), List.of(), List.of(), List.of(), 0)));
        verifyNoInteractions(bindings);
    }

    private FmProcessDefLogicServiceImpl logic() {
        return new FmProcessDefLogicServiceImpl(definitions, null, versions, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, access, null, bindings, groovyAccess, true, null);
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
