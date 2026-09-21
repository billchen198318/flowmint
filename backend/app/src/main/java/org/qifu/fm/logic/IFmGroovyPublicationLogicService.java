package org.qifu.fm.logic;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler.VerifiedPublication;

/** Internal step of the publisher's transaction, after permission and release-gate checks. */
public interface IFmGroovyPublicationLogicService {

    void persist(String tenantId, String versionOid, int expectedLockVersion,
            VerifiedPublication evidence) throws ServiceException;
}
