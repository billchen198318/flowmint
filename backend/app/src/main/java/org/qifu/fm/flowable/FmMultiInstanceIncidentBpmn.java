package org.qifu.fm.flowable;

import java.util.ArrayList;
import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.bpmn.model.ImplementationType;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.UserTask;

/** Adds a recoverable wait path before a multi-instance user task. */
public final class FmMultiInstanceIncidentBpmn {

    private static final String PREFLIGHT_SUFFIX = "__fmMiPreflight";
    private static final String INCIDENT_SUFFIX = "__fmMiIncident";

    private FmMultiInstanceIncidentBpmn() {
    }

    public static String taskKeyFromPreflight(String activityId) {
        if (activityId == null || !activityId.endsWith(PREFLIGHT_SUFFIX)) {
            throw new IllegalArgumentException("Invalid multi-instance preflight activity");
        }
        return activityId.substring(0, activityId.length() - PREFLIGHT_SUFFIX.length());
    }

    public static String taskKeyFromIncident(String activityId) {
        if (activityId == null || !activityId.endsWith(INCIDENT_SUFFIX)) {
            throw new IllegalArgumentException("Invalid multi-instance incident activity");
        }
        return activityId.substring(0, activityId.length() - INCIDENT_SUFFIX.length());
    }

    public static boolean isIncidentTask(String activityId) {
        return activityId != null && activityId.endsWith(INCIDENT_SUFFIX);
    }

    public static boolean isPreflightTask(String activityId) {
        return activityId != null && activityId.endsWith(PREFLIGHT_SUFFIX);
    }

    public static void wrap(BpmnModel model, UserTask task) {
        var process = model.getMainProcess();
        String base = task.getId();
        String preflightId = base + PREFLIGHT_SUFFIX;
        String gatewayId = base + "__fmMiRoute";
        String incidentId = base + INCIDENT_SUFFIX;
        if (process.getFlowElement(preflightId) != null
                || process.getFlowElement(gatewayId) != null
                || process.getFlowElement(incidentId) != null) {
            throw new IllegalStateException("Reserved multi-instance activity ID already exists");
        }
        List<SequenceFlow> incoming = new ArrayList<>(task.getIncomingFlows());
        if (incoming.isEmpty()) {
            throw new IllegalStateException("Multi-instance task has no incoming sequence flow");
        }
        ServiceTask preflight = new ServiceTask();
        preflight.setId(preflightId);
        preflight.setName("Resolve " + base);
        preflight.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        preflight.setImplementation("${fmMultiInstancePreflightDelegate}");
        ExclusiveGateway gateway = new ExclusiveGateway();
        gateway.setId(gatewayId);
        UserTask incident = new UserTask();
        incident.setId(incidentId);
        incident.setName("Assignment Incident: " + base);
        FlowableListener listener = new FlowableListener();
        listener.setEvent("create");
        listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
        listener.setImplementation("${fmMultiInstanceIncidentListener}");
        incident.getTaskListeners().add(listener);

        for (SequenceFlow flow : incoming) {
            flow.setTargetRef(preflightId);
            flow.setTargetFlowElement(preflight);
        }
        SequenceFlow toRoute = flow(preflight, gateway, base + "__fmMiToRoute");
        SequenceFlow toTask = flow(gateway, task, base + "__fmMiToTask");
        toTask.setConditionExpression("${flowmintMiReady}");
        SequenceFlow toIncident = flow(gateway, incident, base + "__fmMiToIncident");
        gateway.setDefaultFlow(toIncident.getId());
        SequenceFlow retry = flow(incident, preflight, base + "__fmMiRetry");
        preflight.setIncomingFlows(new ArrayList<>(incoming));
        preflight.getIncomingFlows().add(retry);
        preflight.setOutgoingFlows(List.of(toRoute));
        gateway.setIncomingFlows(List.of(toRoute));
        gateway.setOutgoingFlows(List.of(toTask, toIncident));
        incident.setIncomingFlows(List.of(toIncident));
        incident.setOutgoingFlows(List.of(retry));
        task.setIncomingFlows(List.of(toTask));
        process.addFlowElement(preflight);
        process.addFlowElement(gateway);
        process.addFlowElement(incident);
        for (SequenceFlow flow : List.of(toRoute, toTask, toIncident, retry)) {
            process.addFlowElement(flow);
        }
    }

    private static SequenceFlow flow(FlowNode source, FlowNode target, String id) {
        SequenceFlow flow = new SequenceFlow(source.getId(), target.getId());
        flow.setId(id);
        flow.setSourceFlowElement(source);
        flow.setTargetFlowElement(target);
        return flow;
    }
}
