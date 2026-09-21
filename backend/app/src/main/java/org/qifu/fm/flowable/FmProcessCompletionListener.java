package org.qifu.fm.flowable;

import java.time.Clock;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.event.FlowableProcessEngineEvent;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.beans.factory.ObjectProvider;

/** Keeps the FlowMint process projection aligned when Flowable finishes asynchronously. */
public class FmProcessCompletionListener implements FlowableEventListener {

    static final String UPDATE_ACCOUNT = "flowable-process-end";

    private final ObjectProvider<IFmProcessInstanceService> processInstances;
    private final Clock clock;

    public FmProcessCompletionListener(ObjectProvider<IFmProcessInstanceService> processInstances) {
        this(processInstances, Clock.systemUTC());
    }

    FmProcessCompletionListener(ObjectProvider<IFmProcessInstanceService> processInstances, Clock clock) {
        this.processInstances = processInstances;
        this.clock = clock;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (event.getType() != FlowableEngineEventType.PROCESS_COMPLETED
                || !(event instanceof FlowableProcessEngineEvent processEvent)) {
            return;
        }
        DelegateExecution execution = processEvent.getExecution();
        if (execution == null || blank(execution.getTenantId()) || blank(execution.getProcessInstanceId())) {
            return;
        }
        processInstances.getObject().updateStatus(
                execution.getTenantId(), execution.getProcessInstanceId(),
                "RUNNING", "COMPLETED", Date.from(clock.instant()), UPDATE_ACCOUNT);
    }

    @Override
    public Collection<? extends FlowableEventType> getTypes() {
        return List.of(FlowableEngineEventType.PROCESS_COMPLETED);
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
