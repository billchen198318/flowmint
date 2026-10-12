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
import org.qifu.fm.domain.authority.FmApprovalAuthorityConditionEvaluator;
import org.qifu.fm.domain.tenant.FmTenantAccessGuard;
import org.qifu.fm.entity.FmApprovalAuthority;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.entity.FmTaskAssignmentRule;
import org.qifu.fm.service.IFmApprovalAuthorityRuleService;
import org.qifu.fm.service.IFmApprovalAuthorityService;
import org.qifu.fm.service.IFmProcessVersionService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;

import tools.jackson.databind.ObjectMapper;

class FmApprovalAuthorityPublishedReferenceTest {

    @Test
    void publishedPrimaryAndFallbackReferencesPreventMutation() {
        IFmProcessVersionService versions = mock(IFmProcessVersionService.class);
        IFmTaskAssignmentRuleService assignments = mock(IFmTaskAssignmentRuleService.class);
        FmApprovalAuthorityLogicServiceImpl logic = new FmApprovalAuthorityLogicServiceImpl(
                mock(IFmApprovalAuthorityService.class),
                mock(IFmApprovalAuthorityRuleService.class),
                mock(FmApprovalAuthorityConditionEvaluator.class),
                mock(FmTenantAccessGuard.class),
                new ObjectMapper(), versions, assignments);
        FmApprovalAuthority authority = new FmApprovalAuthority();
        authority.setTenantId("tenant");
        authority.setProcessDefId("process");
        authority.setApprovalAuthorityId("authority");
        FmProcessVersion version = new FmProcessVersion();
        version.setVersionNo(2);
        version.setVersionStatus("PUBLISHED");
        when(versions.selectListByParams(anyMap())).thenReturn(result(List.of(version)));

        FmTaskAssignmentRule rule = new FmTaskAssignmentRule();
        rule.setProcessVersionNo(2);
        rule.setResolverType("APPROVAL_AUTHORITY");
        rule.setResolverConfig("{\"approvalAuthorityId\":\"authority\"}");
        when(assignments.selectListByParams(anyMap())).thenReturn(result(List.of(rule)));
        assertThrows(ServiceException.class,
                () -> logic.requireUnreferencedByReleasedVersion(authority));

        rule.setResolverType("FIXED_ACCOUNT");
        rule.setResolverConfig("{}");
        rule.setFallbackConfig("{\"resolverType\":\"APPROVAL_AUTHORITY\","
                + "\"resolverConfig\":{\"approvalAuthorityId\":\"authority\"}}");
        assertThrows(ServiceException.class,
                () -> logic.requireUnreferencedByReleasedVersion(authority));

        version.setVersionStatus("DRAFT");
        assertDoesNotThrow(() -> logic.requireUnreferencedByReleasedVersion(authority));
    }

    private static <T> DefaultResult<T> result(T value) {
        DefaultResult<T> result = new DefaultResult<>();
        result.setValue(value);
        return result;
    }
}
