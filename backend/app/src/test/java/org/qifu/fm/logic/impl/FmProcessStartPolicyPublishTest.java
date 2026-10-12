package org.qifu.fm.logic.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.base.model.DefaultResult;
import org.qifu.fm.entity.FmProcessStartPolicy;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.service.IFmEmployeeService;
import org.qifu.fm.service.IFmProcessStartPolicyService;

class FmProcessStartPolicyPublishTest {

    @Test
    void rejectsStaleAllowedAccountButAcceptsAllPolicy() {
        IFmProcessStartPolicyService policies = mock(IFmProcessStartPolicyService.class);
        IFmEmployeeService employees = mock(IFmEmployeeService.class);
        FmProcessDefLogicServiceImpl logic = new FmProcessDefLogicServiceImpl(
                null, null, null, null, null, null, null, policies, null, employees,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, false, null);
        FmProcessVersion version = new FmProcessVersion();
        version.setTenantId("tenant");
        version.setProcessDefId("process");
        version.setVersionNo(1);
        FmProcessStartPolicy policy = new FmProcessStartPolicy();
        policy.setAllowStart("Y");
        policy.setSubjectType("ACCOUNT");
        policy.setSubjectRefId("retired-account");
        when(policies.findByVersion("tenant", "process", 1))
                .thenReturn(List.of(policy));
        when(employees.selectListByParams(anyMap())).thenReturn(result(List.of()));

        assertThrows(ServiceException.class,
                () -> logic.validateStartPoliciesForPublish(version));
        policy.setSubjectType("ALL");
        policy.setSubjectRefId(null);
        assertDoesNotThrow(() -> logic.validateStartPoliciesForPublish(version));
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
