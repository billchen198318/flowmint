package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.qifu.fm.domain.notification.FmNotificationPublisher;
import org.qifu.fm.domain.resolver.IFmAssignmentResolverService;
import org.qifu.fm.entity.FmTaskPolicy;
import org.qifu.fm.logic.IFmRuntimeAuditLogicService;
import org.qifu.fm.service.IFmTaskActionService;
import org.qifu.fm.service.IFmTaskAssignmentRuleService;
import org.qifu.fm.service.IFmTaskPolicyService;

import tools.jackson.databind.ObjectMapper;

class FmMultiInstanceAssignmentFailureTest {

    @Test
    void resolverFailureHappensBeforeTaskAndCannotCreateRecoverableIncident() throws Exception {
        IFmTaskAssignmentRuleService rules = mock(IFmTaskAssignmentRuleService.class);
        IFmTaskPolicyService policies = mock(IFmTaskPolicyService.class);
        FmAssignmentIncidentRecorder incidents = mock(FmAssignmentIncidentRecorder.class);
        FmTaskAssignmentListener listener = new FmTaskAssignmentListener(rules, policies,
                mock(IFmAssignmentResolverService.class),
                mock(IFmRuntimeAuditLogicService.class), incidents,
                mock(FmNotificationPublisher.class), mock(IFmTaskActionService.class),
                new ObjectMapper());
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_TENANT_ID))
                .thenReturn("T1");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID))
                .thenReturn("P1");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO))
                .thenReturn(1);
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT))
                .thenReturn("applicant");
        when(execution.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID))
                .thenReturn("FD1");
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setTaskDefKey("approve");
        policy.setAssignmentMode("ALL");
        when(policies.findByVersion("T1", "P1", 1)).thenReturn(List.of(policy));
        when(rules.findByVersion("T1", "P1", 1)).thenReturn(List.of());

        assertThrows(FlowableException.class,
                () -> listener.multiInstanceAccounts(execution, "approve"));
        verify(incidents, never()).record(any());

        listener.prepareMultiInstance(execution, "approve");
        verify(execution).setVariable(FmTaskAssignmentListener.VARIABLE_MI_READY, false);
        verify(execution, atLeastOnce()).setVariable(
                org.mockito.ArgumentMatchers.eq(FmTaskAssignmentListener.VARIABLE_MI_ERROR),
                any());
    }
}
