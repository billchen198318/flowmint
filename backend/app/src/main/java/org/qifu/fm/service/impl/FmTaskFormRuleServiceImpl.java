package org.qifu.fm.service.impl;

import java.util.List;

import org.qifu.fm.dto.view.FmPublishedFormOptionView;
import org.qifu.fm.entity.FmTaskFormRule;
import org.qifu.fm.mapper.FmTaskFormRuleMapper;
import org.qifu.fm.service.IFmTaskFormRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTaskFormRuleServiceImpl implements IFmTaskFormRuleService {

    private final FmTaskFormRuleMapper mapper;

    public FmTaskFormRuleServiceImpl(FmTaskFormRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<FmTaskFormRule> findByVersion(
            String tenantId, String processDefId, Integer versionNo) {
        return mapper.selectByVersion(tenantId, processDefId, versionNo);
    }

    @Override
    @Transactional(readOnly = false)
    public void replaceVersion(String tenantId, String processDefId,
            Integer versionNo, List<FmTaskFormRule> rules) {
        mapper.deleteByVersion(tenantId, processDefId, versionNo);
        rules.forEach(mapper::insert);
    }

    @Override
    public List<FmPublishedFormOptionView> publishedFormOptions(String tenantId) {
        return mapper.selectPublishedFormOptions(tenantId);
    }

    @Override
    public boolean isPublishedFormVersion(
            String tenantId, String formId, Integer versionNo) {
        return mapper.countPublishedFormVersion(tenantId, formId, versionNo) == 1;
    }
}
