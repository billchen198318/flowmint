package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmFormData;
import org.qifu.fm.mapper.FmFormDataMapper;
import org.qifu.fm.service.IFmFormDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmFormDataServiceImpl extends BaseService<FmFormData, String>
        implements IFmFormDataService {

    private final FmFormDataMapper mapper;

    public FmFormDataServiceImpl(FmFormDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmFormData, String> getBaseMapper() {
        return mapper;
    }
}
