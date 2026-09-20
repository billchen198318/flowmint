package org.qifu.fm.domain.workflow;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmSystemTaskExecution;
import org.qifu.fm.logic.IFmGroovyReadyRecoveryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

class FmGroovyReadyReaperTest {
    @Test
    void bothGatesAndExplicitTenantsAreRequiredBeforeReadingTables() {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyReadyRecoveryLogicService.class);
        new FmGroovyReadyReaper(executions, recovery, true, false, "").reap();
        new FmGroovyReadyReaper(executions, recovery, false, true, "").reap();
        verifyNoInteractions(executions, recovery);
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyReadyReaper(executions, recovery, true, true, ""));
    }

    @Test
    void failureAndForeignRowDoNotBlockRemainingInvocationsOrTenants() throws Exception {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyReadyRecoveryLogicService.class);
        when(executions.findStalledReady(eq("T"), any(), isNull(), eq(100)))
                .thenReturn(List.of(row("T", "first"), row("U", "foreign"), row("T", "second")));
        when(executions.findStalledReady(eq("U"), any(), isNull(), eq(100))).thenReturn(List.of(row("U", "third")));
        doThrow(new ServiceException("failed")).when(recovery).recover("T", "first", 0);
        new FmGroovyReadyReaper(executions, recovery, true, true, "T,T,U").reap();
        verify(recovery).recover("T", "second", 0);
        verify(recovery).recover("U", "third", 0);
        verify(recovery, never()).recover("T", "foreign", 0);
    }

    @Test
    void cursorMovesPastWaitingFirstPageAndWrapsAfterLastPage() throws Exception {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyReadyRecoveryLogicService.class);
        var firstPage = IntStream.range(0, 100).mapToObj(n -> row("T", String.format("%03d", n))).toList();
        when(executions.findStalledReady(eq("T"), any(), isNull(), eq(100))).thenReturn(firstPage);
        when(executions.findStalledReady(eq("T"), any(), eq("099"), eq(100))).thenReturn(List.of(row("T", "100")));
        var reaper = new FmGroovyReadyReaper(executions, recovery, true, true, "T");
        reaper.reap();
        reaper.reap();
        reaper.reap();
        verify(recovery).recover("T", "100", 0);
        verify(executions, times(2)).findStalledReady(eq("T"), any(), isNull(), eq(100));
        verify(executions).findStalledReady(eq("T"), any(), eq("099"), eq(100));
    }

    private FmSystemTaskExecution row(String tenant, String id) {
        var row = new FmSystemTaskExecution();
        row.setOid(id);
        row.setTenantId(tenant);
        row.setInvocationId(id);
        row.setGeneration(0);
        return row;
    }
}
