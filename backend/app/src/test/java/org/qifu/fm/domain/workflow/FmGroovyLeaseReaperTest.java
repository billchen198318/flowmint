package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyRecoveryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

class FmGroovyLeaseReaperTest {

    @Test
    void disabledReaperDoesNotAccessUnmigratedTables() {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyRecoveryLogicService.class);
        new FmGroovyLeaseReaper(executions, recovery, false, "").reap();
        verifyNoInteractions(executions, recovery);
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyLeaseReaper(executions, recovery, true, ""));
    }

    @Test
    void oneFailureDoesNotBlockNextInvocationOrTenantAndForeignRowsAreRejected() throws Exception {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyRecoveryLogicService.class);
        when(executions.findExpired(eq("T"), any(), eq(100)))
                .thenReturn(List.of(row("T", "first"), row("OTHER", "foreign"), row("T", "second")));
        when(executions.findExpired(eq("U"), any(), eq(100))).thenReturn(List.of(row("U", "third")));
        doThrow(new ServiceException("SIMULATED_DB_FAILURE")).when(recovery).expire("T", "first", 1);
        new FmGroovyLeaseReaper(executions, recovery, true, "T,T,U").reap();
        verify(recovery).expire("T", "second", 1);
        verify(recovery).expire("U", "third", 1);
        verify(recovery, never()).expire("T", "foreign", 1);
        verify(recovery, never()).expire("OTHER", "foreign", 1);
    }

    private FmSystemTaskExecution row(String tenantId, String invocationId) {
        var row = new FmSystemTaskExecution();
        row.setTenantId(tenantId);
        row.setInvocationId(invocationId);
        row.setGeneration(1);
        return row;
    }
}
