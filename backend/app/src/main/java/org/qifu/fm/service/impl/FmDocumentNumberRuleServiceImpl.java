package org.qifu.fm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmDocumentNumberRule;
import org.qifu.fm.mapper.FmDocumentNumberRuleMapper;
import org.qifu.fm.service.IFmDocumentNumberRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmDocumentNumberRuleServiceImpl
        extends BaseService<FmDocumentNumberRule, String>
        implements IFmDocumentNumberRuleService {

    private final FmDocumentNumberRuleMapper mapper;

    public FmDocumentNumberRuleServiceImpl(FmDocumentNumberRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmDocumentNumberRule, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public FmDocumentNumberRule selectActive(
            String tenantId, String documentType) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tenantId", tenantId);
        paramMap.put("documentType", documentType);
        return mapper.selectActive(paramMap);
    }
}
