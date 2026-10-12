package org.qifu.fm.domain.dataaction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.qifu.fm.domain.datasource.FmDataSourcePoolRegistry;
import org.qifu.fm.entity.FmDataActionStep;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

class FmDataActionForEachTimeoutTest {

    @Test
    void batchUsesItsOwnStepTimeout() throws Exception {
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        JdbcTemplate core = mock(JdbcTemplate.class);
        when(jdbc.getJdbcTemplate()).thenReturn(core);
        when(jdbc.batchUpdate(anyString(), any(SqlParameterSource[].class)))
                .thenReturn(new int[] {1});
        doAnswer(invocation -> {
            KeyHolder keys = invocation.getArgument(2);
            keys.getKeyList().add(Map.of("id", 7L));
            return 1;
        }).when(jdbc).update(anyString(), any(MapSqlParameterSource.class),
                any(KeyHolder.class));
        FmDataActionParameterResolver resolver = mock(FmDataActionParameterResolver.class);
        when(resolver.readRequestPath(any(), anyString()))
                .thenReturn(List.of(Map.of("itemCode", "A01")));
        when(resolver.resolveForStep(any(), any(), any(), any(), any()))
                .thenReturn(Map.of("itemCode", "A01"));
        FmDataActionContinueConditionEvaluator conditions =
                mock(FmDataActionContinueConditionEvaluator.class);
        when(conditions.evaluate(nullable(String.class), any(), any()))
                .thenReturn(true);
        var executor = new FmDataActionExecutor(
                mock(FmDataSourcePoolRegistry.class), resolver,
                mock(FmDataActionSqlValidator.class),
                mock(FmDataActionExecutionAuditRecorder.class),
                mock(FmDataActionRateLimiter.class), conditions,
                new ObjectMapper());
        FmDataActionStep step = new FmDataActionStep();
        step.setStatus("ACTIVE");
        step.setExecutionMode("FOR_EACH");
        step.setArrayPath("$.items");
        step.setSqlContent("UPDATE items SET seen=1 WHERE code=:itemCode");
        step.setResultMode("NONE");
        step.setQueryTimeoutSeconds(17);
        FmDataActionStep generated = new FmDataActionStep();
        generated.setStatus("ACTIVE");
        generated.setExecutionMode("FOR_EACH");
        generated.setArrayPath("$.items");
        generated.setSqlContent("INSERT INTO items(code) VALUES (:itemCode)");
        generated.setResultMode("GENERATED_KEY");
        generated.setResultKey("keys");
        generated.setQueryTimeoutSeconds(23);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(executor,
                "executeSteps", jdbc, List.of(step, generated), Map.of(),
                Map.of("items", List.of(Map.of("itemCode", "A01"))), "{}"));
        verify(core).setQueryTimeout(17);
        verify(core).setQueryTimeout(23);
    }
}
