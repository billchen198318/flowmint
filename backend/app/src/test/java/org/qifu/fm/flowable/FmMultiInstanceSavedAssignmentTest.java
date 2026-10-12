package org.qifu.fm.flowable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.flowable.task.service.delegate.DelegateTask;
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

class FmMultiInstanceSavedAssignmentTest {

    @Test
    void sequentialTaskUsesPreflightAssigneeAfterRulesChange() throws Exception {
        IFmTaskAssignmentRuleService rules = mock(IFmTaskAssignmentRuleService.class);
        IFmTaskPolicyService policies = mock(IFmTaskPolicyService.class);
        IFmAssignmentResolverService resolver = mock(IFmAssignmentResolverService.class);
        FmAssignmentIncidentRecorder incidents = mock(FmAssignmentIncidentRecorder.class);
        FmTaskAssignmentListener listener = new FmTaskAssignmentListener(rules, policies,
                resolver, mock(IFmRuntimeAuditLogicService.class), incidents,
                mock(FmNotificationPublisher.class), mock(IFmTaskActionService.class),
                new ObjectMapper());
        DelegateTask task = mock(DelegateTask.class);
        when(task.getTaskDefinitionKey()).thenReturn("approve");
        when(task.getProcessInstanceId()).thenReturn("PI1");
        when(task.getId()).thenReturn("TASK2");
        when(task.getAssignee()).thenReturn("second");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_TENANT_ID))
                .thenReturn("T1");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID))
                .thenReturn("P1");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO))
                .thenReturn(1);
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT))
                .thenReturn("applicant");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_FORM_DATA_ID))
                .thenReturn("FD1");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_MI_TASK_KEY))
                .thenReturn("approve");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_MI_ACCOUNTS))
                .thenReturn(List.of("first", "second"));
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_MI_CANDIDATES))
                .thenReturn(List.of(Map.of("employeeId", "E1", "account", "first"),
                        Map.of("employeeId", "E2", "account", "second")));
        when(task.getVariable("flowmintAssignee")).thenReturn("second");
        FmTaskPolicy policy = new FmTaskPolicy();
        policy.setTaskDefKey("approve");
        policy.setAssignmentMode("SEQUENTIAL");
        when(policies.findByVersion("T1", "P1", 1)).thenReturn(List.of(policy));

        listener.notify(task);

        verify(task).setAssignee("second");
        verify(rules, never()).findByVersion(any(), any(), any());
        verify(resolver, never()).resolve(any(), any(), any());
        verify(incidents, never()).record(any());
    }
}
