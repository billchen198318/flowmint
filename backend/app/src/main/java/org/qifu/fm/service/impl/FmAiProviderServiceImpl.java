package org.qifu.fm.service.impl;

import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmAiProvider;
import org.qifu.fm.mapper.FmAiProviderMapper;
import org.qifu.fm.service.IFmAiProviderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmAiProviderServiceImpl extends BaseService<FmAiProvider, String>
		implements IFmAiProviderService {

	private final FmAiProviderMapper mapper;

	public FmAiProviderServiceImpl(FmAiProviderMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmAiProvider, String> getBaseMapper() {
		return mapper;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void lockByTenant(String tenantId) {
		mapper.lockByTenant(tenantId);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void updateTestStatus(FmAiProvider provider, String status, Date testedAt)
			throws ServiceException {
		provider.setUuserid(UserUtils.getCurrentUser().getUsername());
		provider.setUdate(new Date());
		if (mapper.updateTestStatus(provider.getOid(), provider.getTenantId(), status,
				testedAt, provider.getLockVersion(), provider.getUuserid(),
				provider.getUdate()) != 1) {
			throw new ServiceException(BaseSystemMessage.updateFail());
		}
	}
}
