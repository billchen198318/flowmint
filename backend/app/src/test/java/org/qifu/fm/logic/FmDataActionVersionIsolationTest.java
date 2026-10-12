package org.qifu.fm.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.domain.dataaction.FmDataActionExecutor;
import org.qifu.fm.domain.dataaction.FmDataActionSqlValidator;
import org.qifu.fm.dto.command.FmDataActionCommand;
import org.qifu.fm.dto.command.FmDataActionStepCommand;
import org.qifu.fm.dto.view.FmDataActionExecutionView;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;
import org.qifu.fm.entity.FmDataActionVersion;
import org.qifu.fm.entity.FmDataSourcePool;
import org.qifu.fm.logic.impl.FmDataActionLogicServiceImpl;
import org.qifu.fm.service.IFmDataActionService;
import org.qifu.fm.service.IFmDataActionStepService;
import org.qifu.fm.service.IFmDataActionVersionService;
import org.qifu.fm.service.IFmDataSourcePoolService;

import tools.jackson.databind.ObjectMapper;

class FmDataActionVersionIsolationTest {
    private final IFmDataActionService actions = mock(IFmDataActionService.class);
    private final IFmDataActionVersionService versions = mock(IFmDataActionVersionService.class);
    private final IFmDataActionStepService steps = mock(IFmDataActionStepService.class);
    private final IFmDataSourcePoolService pools = mock(IFmDataSourcePoolService.class);
    private final FmDataActionExecutor executor = mock(FmDataActionExecutor.class);
    private final FmDataActionLogicServiceImpl logic = new FmDataActionLogicServiceImpl(
            actions, versions, steps, pools, mock(FmDataActionSqlValidator.class),
            executor, new ObjectMapper());

    @Test
    void publishedExecutionReadsVersionConfigurationAfterMasterChanges() throws Exception {
        FmDataAction action = action();
        action.setPoolId("draft-pool");
        action.setRequestSchema("{\"draft\":\"$.draft\"}");
        FmDataActionVersion published = version("PUBLISHED");
        published.setActionConfig("{\"actionName\":\"Old\",\"poolId\":\"published-pool\","
                + "\"actionType\":\"QUERY\",\"requestSchema\":\"{}\","
                + "\"responseMode\":\"COMPOSITE\",\"status\":\"ACTIVE\","
                + "\"rateLimitPerMinute\":10,\"description\":null}");
        when(actions.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of(action)));
        when(versions.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of(published)));
        when(steps.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of()));
        when(executor.execute(any(), eq(1), any(), any(), eq("user"), eq(false)))
                .thenReturn(new FmDataActionExecutionView("run", "CODE", 1, false, Map.of()));

        logic.execute("tenant", "CODE", 1, Map.of(), "user");

        ArgumentCaptor<FmDataAction> captor = ArgumentCaptor.forClass(FmDataAction.class);
        verify(executor).execute(captor.capture(), eq(1), any(), any(), eq("user"), eq(false));
        assertNotSame(action, captor.getValue());
        assertEquals("published-pool", captor.getValue().getPoolId());
        assertEquals("{}", captor.getValue().getRequestSchema());
    }

    @Test
    void updatingDraftKeepsPublishedMasterConfiguration() throws Exception {
        FmDataAction action = action();
        FmDataActionVersion draft = version("DRAFT");
        draft.setVersionNo(2);
        when(actions.selectByPrimaryKey("oid")).thenReturn(result(action));
        when(actions.updateOptimistic(action, 0)).thenReturn(true);
        when(versions.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of(draft)));
        when(steps.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of()));
        FmDataSourcePool pool = new FmDataSourcePool();
        pool.setTenantId("tenant");
        pool.setPoolId("new-pool");
        pool.setStatus("ACTIVE");
        when(pools.selectListByParams(any(), anyString(), anyString()))
                .thenReturn(result(List.of(pool)));
        FmDataActionCommand command = new FmDataActionCommand("oid", "tenant", "CODE",
                "Changed", "new-pool", "QUERY", "{}", "COMPOSITE", "DRAFT",
                2, 0, 20, null, List.of(new FmDataActionStepCommand(null,
                        "STEP", "Step", 1, "SELECT_ONE", "ONCE", "SELECT 1",
                        null, "result", "OBJECT", null, null, 30, 100,
                        0, 0, "ACTIVE")));

        logic.update(command);

        assertEquals("published-pool", action.getPoolId());
        assertEquals("ACTIVE", action.getStatus());
        assertEquals("new-pool", logic.view(action).poolId());
    }

    private FmDataAction action() {
        FmDataAction action = new FmDataAction();
        action.setOid("oid");
        action.setTenantId("tenant");
        action.setActionId("id");
        action.setActionCode("CODE");
        action.setActionName("Old");
        action.setPoolId("published-pool");
        action.setActionType("QUERY");
        action.setRequestSchema("{}");
        action.setResponseMode("COMPOSITE");
        action.setStatus("ACTIVE");
        action.setCurrentVersionNo(1);
        action.setLockVersion(0);
        return action;
    }

    private FmDataActionVersion version(String status) {
        FmDataActionVersion version = new FmDataActionVersion();
        version.setOid("version-oid");
        version.setTenantId("tenant");
        version.setActionId("id");
        version.setVersionNo(1);
        version.setVersionStatus(status);
        return version;
    }

    private <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
