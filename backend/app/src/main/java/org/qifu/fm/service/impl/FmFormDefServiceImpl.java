package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmFormDef;
import org.qifu.fm.mapper.FmFormDefMapper;
import org.qifu.fm.service.IFmFormDefService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmFormDefServiceImpl extends BaseService<FmFormDef, String>
        implements IFmFormDefService {

    private final FmFormDefMapper mapper;

    public FmFormDefServiceImpl(FmFormDefMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmFormDef, String> getBaseMapper() {
        return mapper;
    }
}
