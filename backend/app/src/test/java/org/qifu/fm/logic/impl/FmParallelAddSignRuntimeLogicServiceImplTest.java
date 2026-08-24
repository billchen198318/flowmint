package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmTaskParallelAddSign;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmTaskParallelAddSignMemberService;
import org.qifu.fm.service.IFmTaskParallelAddSignService;
import org.qifu.fm.service.IFmTaskPolicyService;
import org.qifu.fm.service.IFmTenantAccountService;

class FmParallelAddSignRuntimeLogicServiceImplTest {

    @Test
    void returnsCompletedAndCancelledBatchesForProcessTracking() throws Exception {
        IFmTaskParallelAddSignService batches =
                mock(IFmTaskParallelAddSignService.class);
        IFmTaskParallelAddSignMemberService members =
                mock(IFmTaskParallelAddSignMemberService.class);
        FmTaskParallelAddSign completed = batch("completed", "COMPLETED", 2, 2);
        FmTaskParallelAddSign cancelled = batch("cancelled", "CANCELLED", 3, 0);
        when(batches.findByProcessInstance("tenant", "process"))
                .thenReturn(List.of(completed, cancelled));
        when(members.findByBatch("tenant", "completed")).thenReturn(List.of());
        when(members.findByBatch("tenant", "cancelled")).thenReturn(List.of());
        FmParallelAddSignRuntimeLogicServiceImpl logic =
                new FmParallelAddSignRuntimeLogicServiceImpl(
                        mock(TaskService.class), mock(IFmEmployeeService.class),
                        mock(IFmTenantAccountService.class), mock(IFmTaskPolicyService.class),
                        batches, members, mock(FmParallelAddSignStartService.class),
                        mock(FmParallelAddSignCompleteService.class),
                        mock(FmParallelAddSignCancelService.class));

        var result = logic.processDetails("tenant", "process").getValue();

        assertEquals(2, result.size());
        assertEquals("COMPLETED", result.get(0).status());
        assertEquals("CANCELLED", result.get(1).status());
    }

    private FmTaskParallelAddSign batch(
            String oid, String status, int total, int completed) {
        FmTaskParallelAddSign value = new FmTaskParallelAddSign();
        value.setOid(oid);
        value.setStatus(status);
        value.setTotalCount(total);
        value.setCompletedCount(completed);
        value.setAgreeCount(completed);
        value.setDisagreeCount(0);
        return value;
    }
}
