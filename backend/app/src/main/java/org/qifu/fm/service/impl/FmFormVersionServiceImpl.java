package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmFormVersion;
import org.qifu.fm.mapper.FmFormVersionMapper;
import org.qifu.fm.service.IFmFormVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmFormVersionServiceImpl extends BaseService<FmFormVersion, String>
        implements IFmFormVersionService {

    private final FmFormVersionMapper mapper;

    public FmFormVersionServiceImpl(FmFormVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmFormVersion, String> getBaseMapper() {
        return mapper;
    }
}
