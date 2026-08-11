package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.qifu.base.model.PageOf;
import org.qifu.base.model.QueryResult;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.dto.command.FmProcessMonitorRequest;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.service.IFmFormDataService;
import org.qifu.fm.service.IFmFormSnapshotService;
import org.qifu.fm.service.IFmProcessDefService;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.qifu.fm.service.IFmTaskActionService;

import tools.jackson.databind.ObjectMapper;

class FmProcessMonitorLogicServiceImplTest {

    @Test
    void returnsEmptyPageWhenQifuFindPageUsesNullForNoData() throws Exception {
        IFmProcessInstanceService processes = mock(IFmProcessInstanceService.class);
        QueryResult<List<FmProcessInstance>> emptyQuery = new QueryResult<>();
        when(processes.<FmProcessInstance>findPage(anyMap(), any(PageOf.class)))
                .thenReturn(emptyQuery);
        FmProcessMonitorLogicServiceImpl logic = new FmProcessMonitorLogicServiceImpl(
                mock(TaskService.class), processes, mock(IFmProcessDefService.class),
                mock(IFmFormDataService.class), mock(IFmTaskActionService.class),
                mock(IFmFormSnapshotService.class), mock(ObjectMapper.class));

        try (MockedStatic<UserUtils> users = mockStatic(UserUtils.class)) {
            users.when(UserUtils::isAdmin).thenReturn(true);
            var result = logic.find("T001",
                    new FmProcessMonitorRequest("RUNNING", null, 1, 30));

            assertEquals(List.of(), result.getValue().items());
            assertEquals(0L, result.getValue().totalCount());
            assertEquals(1, result.getValue().totalPages());
            assertEquals(1, result.getValue().page());
            assertEquals(30, result.getValue().pageSize());
        }
    }
}
