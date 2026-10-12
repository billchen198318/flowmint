package org.qifu.fm.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("fmMultiInstancePreflightDelegate")
public class FmMultiInstancePreflightDelegate implements JavaDelegate {

    private final FmTaskAssignmentListener assignmentListener;

    public FmMultiInstancePreflightDelegate(FmTaskAssignmentListener assignmentListener) {
        this.assignmentListener = assignmentListener;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String taskKey = FmMultiInstanceIncidentBpmn.taskKeyFromPreflight(
                execution.getCurrentFlowElement().getId());
        assignmentListener.prepareMultiInstance(execution, taskKey);
    }
}
