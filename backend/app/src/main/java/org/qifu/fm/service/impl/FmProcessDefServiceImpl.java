package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessDef;
import org.qifu.fm.mapper.FmProcessDefMapper;
import org.qifu.fm.service.IFmProcessDefService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmProcessDefServiceImpl extends BaseService<FmProcessDef, String>
        implements IFmProcessDefService {

    private final FmProcessDefMapper mapper;

    public FmProcessDefServiceImpl(FmProcessDefMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessDef, String> getBaseMapper() {
        return mapper;
    }
}