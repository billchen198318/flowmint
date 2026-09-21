package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;

/** Internal recovery entry; callers supply persisted identity, never worker output. */
public interface IFmGroovyRecoveryLogicService {

    boolean expire(String tenantId, String invocationId, int expectedGeneration) throws ServiceException;
}
