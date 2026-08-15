package org.qifu.fm.service;

import java.util.Date;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmDocumentSequence;

public interface IFmDocumentSequenceService
        extends IBaseService<FmDocumentSequence, String> {

    FmDocumentSequence selectForUpdate(
            String tenantId, String documentType, String periodKey);

    int insertInitial(FmDocumentSequence value);

    int increment(
            String tenantId, String oid, Long lockVersion,
            String account, Date now);
}
