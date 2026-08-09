package org.qifu.fm.domain.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.fm.entity.FmProcessStartProxy;

class FmProcessStartProxyEvaluatorTest {

    private final FmProcessStartProxyEvaluator evaluator =
            new FmProcessStartProxyEvaluator();
    private final Date now = new Date();

    @Test
    void allowsSelfSubmissionWithoutProxy() {
        assertTrue(evaluator.isAuthorized("tester", "tester", "P1", List.of(), now));
    }

    @Test
    void deniesProxySubmissionWithoutAuthorization() {
        assertFalse(evaluator.isAuthorized("assistant", "boss", "P1", List.of(), now));
    }

    @Test
    void supportsAllAndProcessScope() {
        assertTrue(evaluator.isAuthorized(
                "assistant", "boss", "P1", List.of(proxy("ALL", null)), now));
        assertTrue(evaluator.isAuthorized(
                "assistant", "boss", "P1", List.of(proxy("PROCESS", "P1")), now));
        assertFalse(evaluator.isAuthorized(
                "assistant", "boss", "P2", List.of(proxy("PROCESS", "P1")), now));
    }

    @Test
    void rejectsInactiveOrExpiredAuthorization() {
        FmProcessStartProxy value = proxy("ALL", null);
        value.setStatus("INACTIVE");
        assertFalse(evaluator.isAuthorized("assistant", "boss", "P1", List.of(value), now));
        value.setStatus("ACTIVE");
        value.setEffectiveTo(new Date(now.getTime() - 1));
        assertFalse(evaluator.isAuthorized("assistant", "boss", "P1", List.of(value), now));
    }

    private FmProcessStartProxy proxy(String scopeType, String scopeRefId) {
        FmProcessStartProxy value = new FmProcessStartProxy();
        value.setStatus("ACTIVE");
        value.setScopeType(scopeType);
        value.setScopeRefId(scopeRefId);
        value.setEffectiveFrom(new Date(now.getTime() - 60_000));
        value.setEffectiveTo(new Date(now.getTime() + 60_000));
        return value;
    }
}
