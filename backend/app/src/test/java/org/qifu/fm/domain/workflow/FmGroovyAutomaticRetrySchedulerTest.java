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
import org.qifu.fm.logic.IFmGroovyAutomaticRetryLogicService;
import org.qifu.fm.service.IFmSystemTaskExecutionService;

class FmGroovyAutomaticRetrySchedulerTest {
    @Test
    void bothGatesAndExplicitTenantsAreRequiredBeforeReadingTables() {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyAutomaticRetryLogicService.class);
        new FmGroovyAutomaticRetryScheduler(executions, recovery, true, false, "").retryDue();
        new FmGroovyAutomaticRetryScheduler(executions, recovery, false, true, "").retryDue();
        verifyNoInteractions(executions, recovery);
        assertThrows(IllegalArgumentException.class, () -> new FmGroovyAutomaticRetryScheduler(executions, recovery, true, true, ""));
    }

    @Test
    void failureAndForeignRowDoNotBlockRemainingInvocationsOrTenants() throws Exception {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyAutomaticRetryLogicService.class);
        when(executions.findAutomaticRetries(eq("T"), any(), isNull(), eq(100)))
                .thenReturn(List.of(row("T", "first"), row("U", "foreign"), row("T", "second")));
        when(executions.findAutomaticRetries(eq("U"), any(), isNull(), eq(100))).thenReturn(List.of(row("U", "third")));
        doThrow(new ServiceException("failed")).when(recovery).retry("T", "first", 1);
        new FmGroovyAutomaticRetryScheduler(executions, recovery, true, true, "T,T,U").retryDue();
        verify(recovery).retry("T", "second", 1);
        verify(recovery).retry("U", "third", 1);
        verify(recovery, never()).retry("T", "foreign", 1);
    }

    @Test
    void cursorMovesPastWaitingFirstPageAndWrapsAfterLastPage() throws Exception {
        var executions = mock(IFmSystemTaskExecutionService.class);
        var recovery = mock(IFmGroovyAutomaticRetryLogicService.class);
        var firstPage = IntStream.range(0, 100).mapToObj(n -> row("T", String.format("%03d", n))).toList();
        when(executions.findAutomaticRetries(eq("T"), any(), isNull(), eq(100))).thenReturn(firstPage);
        when(executions.findAutomaticRetries(eq("T"), any(), eq("099"), eq(100))).thenReturn(List.of(row("T", "100")));
        var reaper = new FmGroovyAutomaticRetryScheduler(executions, recovery, true, true, "T");
        reaper.retryDue();
        reaper.retryDue();
        reaper.retryDue();
        verify(recovery).retry("T", "100", 1);
        verify(executions, times(2)).findAutomaticRetries(eq("T"), any(), isNull(), eq(100));
        verify(executions).findAutomaticRetries(eq("T"), any(), eq("099"), eq(100));
    }

    private FmSystemTaskExecution row(String tenant, String id) {
        var row = new FmSystemTaskExecution();
        row.setOid(id);
        row.setTenantId(tenant);
        row.setInvocationId(id);
        row.setGeneration(1);
        return row;
    }
}
