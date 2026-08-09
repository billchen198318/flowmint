package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmProcessStartPolicy;

public interface IFmProcessStartPolicyService
        extends IBaseService<FmProcessStartPolicy, String> {

    List<FmProcessStartPolicy> findByVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo);

    void replaceVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo,
            List<FmProcessStartPolicy> policies);
}
