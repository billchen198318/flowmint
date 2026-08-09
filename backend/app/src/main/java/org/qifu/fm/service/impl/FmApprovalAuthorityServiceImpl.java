package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmApprovalAuthority;
import org.qifu.fm.mapper.FmApprovalAuthorityMapper;
import org.qifu.fm.service.IFmApprovalAuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FmApprovalAuthorityServiceImpl extends BaseService<FmApprovalAuthority, String>
        implements IFmApprovalAuthorityService {

    private final FmApprovalAuthorityMapper mapper;

    public FmApprovalAuthorityServiceImpl(FmApprovalAuthorityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected IBaseMapper<FmApprovalAuthority, String> getBaseMapper() {
        return mapper;
    }
}
