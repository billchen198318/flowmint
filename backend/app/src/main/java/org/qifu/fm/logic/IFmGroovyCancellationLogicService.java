package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;

public interface IFmGroovyCancellationLogicService {

    /** Called only after the authorized caller transitions the process to a terminal state. */
    void cancelForProcess(String tenantId, String processInstanceId) throws ServiceException;

    /** Internal engine deletion callback; must join the engine transaction. */
    void cancelForExecution(String tenantId, String processInstanceId, String executionId) throws ServiceException;
}
