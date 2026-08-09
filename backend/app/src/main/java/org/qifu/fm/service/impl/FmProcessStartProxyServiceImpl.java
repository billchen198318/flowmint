package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessStartProxy;
import org.qifu.fm.mapper.FmProcessStartProxyMapper;
import org.qifu.fm.service.IFmProcessStartProxyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmProcessStartProxyServiceImpl
        extends BaseService<FmProcessStartProxy, String>
        implements IFmProcessStartProxyService {

    private final FmProcessStartProxyMapper mapper;

    public FmProcessStartProxyServiceImpl(FmProcessStartProxyMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessStartProxy, String> getBaseMapper() {
        return mapper;
    }
}
