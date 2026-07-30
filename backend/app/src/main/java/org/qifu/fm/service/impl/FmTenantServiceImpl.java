package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTenant;
import org.qifu.fm.mapper.FmTenantMapper;
import org.qifu.fm.service.IFmTenantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTenantServiceImpl extends BaseService<FmTenant, String> implements IFmTenantService {
	private final FmTenantMapper tenantMapper;

	public FmTenantServiceImpl(FmTenantMapper tenantMapper) {
		this.tenantMapper = tenantMapper;
	}

	@Override
	protected IBaseMapper<FmTenant, String> getBaseMapper() {
		return tenantMapper;
	}
}