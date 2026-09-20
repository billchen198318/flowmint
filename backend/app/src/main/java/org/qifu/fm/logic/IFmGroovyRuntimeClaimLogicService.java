package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyJobIdentity;

public interface IFmGroovyRuntimeClaimLogicService {

    Claim begin(FmGroovyJobIdentity identity) throws ServiceException;

    void fail(Claim claim, String errorCode) throws ServiceException;

    record Claim(String tenantId, String executionOid, String invocationId, String attemptId, int generation) {
    }
}
