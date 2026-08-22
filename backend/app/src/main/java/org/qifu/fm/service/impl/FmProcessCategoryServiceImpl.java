package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmProcessCategory;
import org.qifu.fm.mapper.FmProcessCategoryMapper;
import org.qifu.fm.service.IFmProcessCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmProcessCategoryServiceImpl
        extends BaseService<FmProcessCategory, String>
        implements IFmProcessCategoryService {

    private final FmProcessCategoryMapper mapper;

    public FmProcessCategoryServiceImpl(FmProcessCategoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmProcessCategory, String> getBaseMapper() {
        return mapper;
    }
}
