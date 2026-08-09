package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessInstance;
import org.qifu.fm.mapper.FmProcessInstanceMapper;
import org.qifu.fm.service.IFmProcessInstanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessInstanceServiceImpl extends BaseService<FmProcessInstance, String>
        implements IFmProcessInstanceService {

    private final FmProcessInstanceMapper mapper;

    public FmProcessInstanceServiceImpl(FmProcessInstanceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessInstance, String> getBaseMapper() {
        return mapper;
    }
}
