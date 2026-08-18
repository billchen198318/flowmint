package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmDocumentSequence;
import org.qifu.fm.mapper.FmDocumentSequenceMapper;
import org.qifu.fm.service.IFmDocumentSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmDocumentSequenceServiceImpl
        extends BaseService<FmDocumentSequence, String>
        implements IFmDocumentSequenceService {

    private final FmDocumentSequenceMapper mapper;

    public FmDocumentSequenceServiceImpl(FmDocumentSequenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmDocumentSequence, String> getBaseMapper() {
        return mapper;
    }

    @Override
    public FmDocumentSequence selectForUpdate(
            String tenantId, String documentType, String periodKey) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tenantId", tenantId);
        paramMap.put("documentType", documentType);
        paramMap.put("periodKey", periodKey);
        return mapper.selectForUpdate(paramMap);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int insertInitial(FmDocumentSequence value) {
        return mapper.insertInitial(value);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public int increment(
            String tenantId, String oid, Long lockVersion,
            String account, Date now) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("oid", oid);
        paramMap.put("tenantId", tenantId);
        paramMap.put("lockVersion", lockVersion);
        paramMap.put("account", account);
        paramMap.put("now", now);
        return mapper.increment(paramMap);
    }
}
