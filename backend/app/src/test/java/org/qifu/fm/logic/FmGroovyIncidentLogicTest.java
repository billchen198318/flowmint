package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyOperateAccess;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.impl.FmGroovyIncidentLogicServiceImpl;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

import tools.jackson.databind.json.JsonMapper;

class FmGroovyIncidentLogicTest {

    private final FmGroovyOperateAccess access = mock(FmGroovyOperateAccess.class);
    private final IFmSystemTaskExecutionService executions = mock(IFmSystemTaskExecutionService.class);
    private final FmGroovyIncidentLogicServiceImpl logic =
            new FmGroovyIncidentLogicServiceImpl(access, executions, true, false);

    @Test
    void unauthorizedOrDisabledNeverReadsLedger() throws Exception {
        assertThrows(ServiceException.class,
                () -> new FmGroovyIncidentLogicServiceImpl(access, executions, false, false).find("T", 0));
        doThrow(new ServiceException("DENIED")).when(access).require("T");
        assertThrows(ServiceException.class, () -> logic.find("T", 0));
        assertThrows(ServiceException.class, () -> logic.detail("T", "invocation"));
        verifyNoInteractions(executions);
    }

    @Test
    void pageIsBoundedAndSerializationContainsNoWorkerOrInputContent() throws Exception {
        var row = row();
        row.setInputContent("SECRET_INPUT");
        row.setContextContent("SECRET_CONTEXT");
        row.setErrorCode("SECRET_ERROR");
        when(executions.findIncidents("T", null, 0, 51)).thenReturn(Collections.nCopies(51, row));
        var page = logic.find("T", 0).getValue();
        assertEquals(50, page.items().size());
        assertTrue(page.hasMore());
        assertFalse(JsonMapper.builder().build().writeValueAsString(page).contains("SECRET"));
        assertThrows(ServiceException.class, () -> logic.find("T", -1));
        assertThrows(ServiceException.class, () -> logic.find("T", 10050));
    }

    @Test
    void foreignRowsAndMissingOrWrongInvocationAreRejected() throws Exception {
        var row = row();
        row.setTenantId("OTHER");
        when(executions.findIncidents(any(), any(), anyInt(), anyInt())).thenReturn(List.of(row));
        assertThrows(ServiceException.class, () -> logic.find("T", 0));
        assertThrows(ServiceException.class, () -> logic.detail("T", "invocation"));
        row.setTenantId("T");
        assertThrows(ServiceException.class, () -> logic.detail("T", "another"));
        when(executions.findIncidents(any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        assertThrows(ServiceException.class, () -> logic.detail("T", "invocation"));
    }

    @Test
    void legacyFailureWithoutIncidentIdUsesStableExecutionIdentity() throws Exception {
        var row = row();
        row.setErrorCode("GROOVY_LEASE_EXPIRED");
        when(executions.findIncidents("T", "invocation", 0, 2)).thenReturn(List.of(row));
        var view = logic.detail("T", "invocation").getValue();
        assertEquals("ledger", view.incidentId());
        assertEquals("執行租約到期", view.failureCategory());
    }

    @Test
    void successfulRecalculationRemainsVisibleWithParentLinkAndNoInventedFailure() throws Exception {
        var row = row();
        row.setParentInvocationId("original");
        row.setStatus("SUCCEEDED");
        when(executions.findIncidents("T", "invocation", 0, 2)).thenReturn(List.of(row));
        var view = logic.detail("T", "invocation").getValue();
        assertEquals("original", view.parentInvocationId());
        assertEquals("SUCCEEDED", view.executionStatus());
        assertEquals("由原異常建立的重算工作", view.failureCategory());
    }

    private FmSystemTaskExecution row() {
        var row = new FmSystemTaskExecution();
        row.setOid("ledger");
        row.setTenantId("T");
        row.setInvocationId("invocation");
        row.setStatus("FAILED");
        return row;
    }
}
