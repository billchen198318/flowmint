package org.qifu.fm.flowable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.flowable.task.service.delegate.DelegateTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;

import tools.jackson.databind.ObjectMapper;

class FmMultiInstanceIncidentListenerTest {

    @Test
    void recordsRealWaitTaskAndOriginalApprovalKey() {
        FmAssignmentIncidentRecorder recorder = mock(FmAssignmentIncidentRecorder.class);
        DelegateTask task = mock(DelegateTask.class);
        when(task.getTaskDefinitionKey()).thenReturn("approval__fmMiIncident");
        when(task.getId()).thenReturn("TASK-1");
        when(task.getProcessInstanceId()).thenReturn("PI-1");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_TENANT_ID))
                .thenReturn("T1");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT))
                .thenReturn("applicant");
        when(task.getVariable(FmTaskAssignmentListener.VARIABLE_MI_ERROR))
                .thenReturn("No candidates");
        when(recorder.record(org.mockito.ArgumentMatchers.any())).thenReturn("INC-1");

        new FmMultiInstanceIncidentListener(recorder, new ObjectMapper()).notify(task);

        ArgumentCaptor<FmAssignmentIncidentRecorder.IncidentCommand> command =
                ArgumentCaptor.forClass(FmAssignmentIncidentRecorder.IncidentCommand.class);
        verify(recorder).record(command.capture());
        assertEquals("TASK-1", command.getValue().taskId());
        assertEquals("approval", command.getValue().taskDefKey());
        verify(task).setCategory("FLOWMINT_INCIDENT:INC-1");
    }
}
