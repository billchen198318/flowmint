package org.qifu.fm.logic;

import org.flowable.engine.delegate.DelegateExecution;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.logic.IFmGroovyRuntimeEvaluationLogicService.Evaluation;

public interface IFmGroovyRuntimeCommitLogicService {

    void apply(DelegateExecution execution, Evaluation evaluation) throws ServiceException;
}
