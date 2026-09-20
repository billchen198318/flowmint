package org.qifu.fm.logic;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler.VerifiedPublication;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;

public interface IFmGroovyPublishCoordinatorLogicService {

    void requireAccess(String tenantId) throws ServiceException;

    VerifiedPublication compile(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> bindings, String formSchema) throws ServiceException;

    <T> T commit(String tenantId, String versionOid, int expectedLockVersion,
            VerifiedPublication evidence, PublicationWork<T> work) throws ServiceException;

    <T> T transaction(PublicationWork<T> work) throws ServiceException;

    @FunctionalInterface
    interface PublicationWork<T> {
        T execute() throws ServiceException;
    }
}
