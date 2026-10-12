package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.datasource.FmDataSourcePoolRegistry;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.entity.FmDataActionStep;

import tools.jackson.databind.ObjectMapper;

class FmDataActionPreflightAuditTest {

    @Test
    void parameterFailureIsAuditedForExecuteAndStream() throws Exception {
        FmDataActionParameterResolver resolver = mock(FmDataActionParameterResolver.class);
        FmDataActionExecutionAuditRecorder audit = mock(FmDataActionExecutionAuditRecorder.class);
        ServiceException failure = new ServiceException("參數錯誤");
        FmDataAction action = action();
        when(resolver.resolve(any(), any(), eq("tenant"), eq("user")))
                .thenThrow(failure);
        FmDataActionExecutor executor = executor(resolver,
                mock(FmDataSourcePoolRegistry.class), audit);
        List<FmDataActionStep> steps = List.of(new FmDataActionStep());

        assertThrows(ServiceException.class, () -> executor.execute(
                action, 1, steps, Map.of(), "user", false));
        assertThrows(ServiceException.class, () -> executor.stream(
                action, 1, steps, Map.of(), "user", new ByteArrayOutputStream()));
        org.mockito.Mockito.verify(audit, org.mockito.Mockito.times(2)).record(
                anyString(), eq(action), eq(1), eq("user"), eq(false), eq(1),
                eq(0), any(Instant.class), eq(failure));
    }

    @Test
    void unavailablePoolIsAuditedBeforeExecution() throws Exception {
        FmDataActionParameterResolver resolver = mock(FmDataActionParameterResolver.class);
        FmDataSourcePoolRegistry pools = mock(FmDataSourcePoolRegistry.class);
        FmDataActionExecutionAuditRecorder audit = mock(FmDataActionExecutionAuditRecorder.class);
        FmDataAction action = action();
        when(resolver.resolve(any(), any(), eq("tenant"), eq("user")))
                .thenReturn(Map.of("tenantId", "tenant"));
        ServiceException failure = new ServiceException("Pool 不存在");
        when(pools.get("tenant", "pool")).thenThrow(failure);
        FmDataActionExecutor executor = executor(resolver, pools, audit);

        assertThrows(ServiceException.class, () -> executor.execute(
                action, 1, List.of(new FmDataActionStep()), Map.of(), "user", false));
        verify(audit).record(anyString(), eq(action), eq(1), eq("user"),
                eq(false), eq(1), eq(1), any(Instant.class), eq(failure));
    }

    private FmDataActionExecutor executor(FmDataActionParameterResolver resolver,
            FmDataSourcePoolRegistry pools, FmDataActionExecutionAuditRecorder audit) {
        return new FmDataActionExecutor(pools, resolver,
                mock(FmDataActionSqlValidator.class), audit,
                mock(FmDataActionRateLimiter.class),
                mock(FmDataActionContinueConditionEvaluator.class), new ObjectMapper());
    }

    private FmDataAction action() {
        FmDataAction action = new FmDataAction();
        action.setTenantId("tenant");
        action.setPoolId("pool");
        action.setActionType("QUERY");
        action.setActionCode("ACTION");
        action.setRequestSchema("{}");
        return action;
    }
}
