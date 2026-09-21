package org.qifu.fm.service;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmProcessVersion;

public interface IFmProcessVersionService extends IBaseService<FmProcessVersion, String> {

    Integer findLockVersion(String tenantId, String oid);

    Integer findDraftLockVersion(String tenantId, String oid);

    Integer lockDraft(String tenantId, String oid);

    int advanceDraftLock(String tenantId, String oid, Integer expectedLockVersion);
}
