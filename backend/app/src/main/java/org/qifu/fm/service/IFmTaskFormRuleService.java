package org.qifu.fm.service;

import java.util.List;

import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.entity.FmTaskFormRule;

public interface IFmTaskFormRuleService {

    List<FmTaskFormRule> findByVersion(
            String tenantId, String processDefId, Integer versionNo);

    void replaceVersion(
            String tenantId, String processDefId, Integer versionNo,
            List<FmTaskFormRule> rules);

    List<FmPublishedFormOptionView> publishedFormOptions(String tenantId);

    boolean isPublishedFormVersion(
            String tenantId, String formId, Integer formVersionNo);
}
