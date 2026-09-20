package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;

public interface IFmGroovyAutomaticRetryLogicService {

    boolean retry(String tenantId, String invocationId, int expectedGeneration) throws ServiceException;
}
