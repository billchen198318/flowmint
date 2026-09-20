package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyRuntimeMapping.ApplyPlan;

/** Evaluates an already committed claim. The future coordinator owns claim, commit and failure recording. */
public interface IFmGroovyRuntimeEvaluationLogicService {

    Evaluation evaluate(String tenantId, String invocationId, String attemptId) throws ServiceException;

    /** A plan, never a success receipt. All fences must be rechecked inside the Flowable commit transaction. */
    record Evaluation(String tenantId, String executionOid, String invocationId, String attemptId,
            int generation, String flowableExecutionId, String processInstanceId, String nodeId,
            String formDataId, int inputRevisionNo, int expectedFormLock, String inputSha256,
            String bindingSha256, String manifestSha256, String resultJson, ApplyPlan plan) {
    }
}
