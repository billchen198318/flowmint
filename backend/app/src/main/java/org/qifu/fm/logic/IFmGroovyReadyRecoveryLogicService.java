package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;

public interface IFmGroovyReadyRecoveryLogicService {
    int GRACE_SECONDS = 120;
    boolean recover(String tenantId, String invocationId, int expectedGeneration) throws ServiceException;
}
