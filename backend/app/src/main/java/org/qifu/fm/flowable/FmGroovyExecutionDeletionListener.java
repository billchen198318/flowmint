package org.qifu.fm.flowable;

import java.util.Collection;
import java.util.List;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyFlowableTransactionGuard;
import org.qifu.fm.logic.IFmGroovyCancellationLogicService;
import org.springframework.beans.factory.ObjectProvider;

/** Synchronous callback: deletion and ledger cancellation either both commit or both roll back. */
public class FmGroovyExecutionDeletionListener implements FlowableEventListener {

    private final ObjectProvider<IFmGroovyCancellationLogicService> cancellation;
    private final ObjectProvider<FmGroovyFlowableTransactionGuard> guard;

    public FmGroovyExecutionDeletionListener(ObjectProvider<IFmGroovyCancellationLogicService> cancellation,
            ObjectProvider<FmGroovyFlowableTransactionGuard> guard) {
        this.cancellation = cancellation;
        this.guard = guard;
    }

    @Override
    public void onEvent(FlowableEvent event) {
        if (event.getType() != FlowableEngineEventType.ENTITY_DELETED
                || !(event instanceof FlowableEntityEvent entityEvent)
                || !(entityEvent.getEntity() instanceof ExecutionEntity execution)
                || execution.getTenantId() == null || execution.getTenantId().isBlank()) {
            return;
        }
        try {
            guard.getObject().require();
            cancellation.getObject().cancelForExecution(execution.getTenantId(),
                    execution.getProcessInstanceId(), execution.getId());
        } catch (ServiceException failure) {
            throw new IllegalStateException("GROOVY_EXECUTION_CANCELLATION_FAILED");
        }
    }

    @Override
    public Collection<? extends FlowableEventType> getTypes() {
        return List.of(FlowableEngineEventType.ENTITY_DELETED);
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
}
