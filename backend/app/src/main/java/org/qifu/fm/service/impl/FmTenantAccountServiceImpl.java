package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmTenantAccount;
import org.qifu.fm.mapper.FmTenantAccountMapper;
import org.qifu.fm.service.IFmTenantAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmTenantAccountServiceImpl extends BaseService<FmTenantAccount, String>
		implements IFmTenantAccountService {
	private final FmTenantAccountMapper tenantAccountMapper;

	public FmTenantAccountServiceImpl(FmTenantAccountMapper tenantAccountMapper) {
		this.tenantAccountMapper = tenantAccountMapper;
	}

	@Override
	protected IBaseMapper<FmTenantAccount, String> getBaseMapper() {
		return tenantAccountMapper;
	}
}