package org.qifu.fm.logic.impl;

import java.util.List;

import org.qifu.base.exception.ServiceException;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler.VerifiedPublication;
import org.qifu.fm.domain.workflow.FmGroovyPublicationCompiler;
import org.qifu.fm.domain.workflow.FmGroovyPublishAccess;
import org.qifu.fm.domain.workflow.FmGroovyPreviewRunner;
import org.qifu.fm.dto.command.FmGroovyBindingCommand;
import org.qifu.fm.entity.FmProcessVersion;
import org.qifu.fm.logic.IFmGroovyPublicationLogicService;
import org.qifu.fm.logic.IFmGroovyPublishCoordinatorLogicService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Compilation precedes the short transaction that persists, deploys and publishes. */
@Service
public class FmGroovyPublishCoordinatorLogicServiceImpl implements IFmGroovyPublishCoordinatorLogicService {

    private final FmGroovyPublishAccess access;
    private final FmGroovyPublicationCompiler compiler;
    private final FmGroovyPreviewRunner runner;
    private final IFmGroovyPublicationLogicService publication;
    private final TransactionTemplate transaction;

    public FmGroovyPublishCoordinatorLogicServiceImpl(FmGroovyPublishAccess access, FmGroovyPublicationCompiler compiler,
            FmGroovyPreviewRunner runner, IFmGroovyPublicationLogicService publication,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager) {
        this.access = access;
        this.compiler = compiler;
        this.runner = runner;
        this.publication = publication;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public VerifiedPublication compile(FmProcessVersion version, String processKey,
            List<FmGroovyBindingCommand> bindings, String formSchema) throws ServiceException {
        access.require(version.getTenantId());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new ServiceException("GROOVY_COMPILE_TRANSACTION_ACTIVE");
        }
        return compiler.compile(version, processKey, bindings, formSchema);
    }

    public void requireAccess(String tenantId) throws ServiceException {
        access.require(tenantId);
    }

    public <T> T commit(String tenantId, String versionOid, int expectedLockVersion,
            VerifiedPublication evidence, PublicationWork<T> work) throws ServiceException {
        access.require(tenantId);
        return transaction(() -> {
            access.require(tenantId);
            if (evidence == null || !evidence.profile().equals(runner.profile())) {
                throw new ServiceException("GROOVY_PUBLICATION_PROFILE_CHANGED");
            }
            publication.persist(tenantId, versionOid, expectedLockVersion, evidence);
            return work.execute();
        });
    }

    public <T> T transaction(PublicationWork<T> work) throws ServiceException {
        try {
            return transaction.execute(status -> {
                try {
                    return work.execute();
                } catch (ServiceException failure) {
                    throw new PublicationFailure(failure);
                }
            });
        } catch (PublicationFailure failure) {
            throw failure.serviceException;
        }
    }

    private static final class PublicationFailure extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final ServiceException serviceException;

        private PublicationFailure(ServiceException serviceException) {
            super(serviceException);
            this.serviceException = serviceException;
        }
    }
}
