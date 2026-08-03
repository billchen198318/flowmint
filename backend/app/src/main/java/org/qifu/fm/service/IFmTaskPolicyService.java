package org.qifu.fm.service;

import java.util.List;

import org.qifu.base.service.IBaseService;
import org.qifu.fm.entity.FmTaskPolicy;

public interface IFmTaskPolicyService extends IBaseService<FmTaskPolicy, String> {

    List<FmTaskPolicy> findByVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo);

    void replaceVersion(
            String tenantId,
            String processDefId,
            Integer processVersionNo,
            List<FmTaskPolicy> policies);
}
