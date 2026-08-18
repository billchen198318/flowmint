package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmDocumentNumberRule;

public interface IFmDocumentNumberRuleService
        extends IBaseService<FmDocumentNumberRule, String> {

    FmDocumentNumberRule selectActive(String tenantId, String documentType);
}
