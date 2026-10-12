package org.qifu.fm.flowable;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.qifu.fm.domain.incident.FmAssignmentIncidentRecorder;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component("fmMultiInstanceIncidentListener")
public class FmMultiInstanceIncidentListener implements TaskListener {

    private final FmAssignmentIncidentRecorder recorder;
    private final ObjectMapper objectMapper;

    public FmMultiInstanceIncidentListener(FmAssignmentIncidentRecorder recorder,
            ObjectMapper objectMapper) {
        this.recorder = recorder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(DelegateTask task) {
        String originalTaskKey = FmMultiInstanceIncidentBpmn.taskKeyFromIncident(
                task.getTaskDefinitionKey());
        String tenantId = value(task, FmTaskAssignmentListener.VARIABLE_TENANT_ID);
        String initiator = value(task, FmTaskAssignmentListener.VARIABLE_INITIATOR_ACCOUNT);
        String incidentId = recorder.record(new FmAssignmentIncidentRecorder.IncidentCommand(
                tenantId, task.getProcessInstanceId(), task.getId(), originalTaskKey,
                "ASSIGNMENT", "RESOLVER_FAILED",
                value(task, FmTaskAssignmentListener.VARIABLE_MI_ERROR),
                objectMapper.writeValueAsString(Map.of(
                        "processDefId", value(task,
                                FmTaskAssignmentListener.VARIABLE_PROCESS_DEF_ID),
                        "processVersionNo", value(task,
                                FmTaskAssignmentListener.VARIABLE_PROCESS_VERSION_NO),
                        "initiatorAccount", initiator,
                        "multiInstancePreflight", true)),
                initiator, new Date()));
        task.setCategory("FLOWMINT_INCIDENT:" + incidentId);
    }

    private String value(DelegateTask task, String name) {
        return Objects.toString(task.getVariable(name), "");
    }
}
