package org.qifu.fm.service.impl;

import java.util.Date;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.message.BaseSystemMessage;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmApiClientKey;
import org.qifu.fm.mapper.FmApiClientKeyMapper;
import org.qifu.fm.service.IFmApiClientKeyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmApiClientKeyServiceImpl extends BaseService<FmApiClientKey, String>
		implements IFmApiClientKeyService {
	private final FmApiClientKeyMapper mapper;

	public FmApiClientKeyServiceImpl(FmApiClientKeyMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmApiClientKey, String> getBaseMapper() { return mapper; }

	@Override
	public FmApiClientKey selectByKeyId(String keyId) { return mapper.selectByKeyId(keyId); }

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void revoke(FmApiClientKey key, String reason) throws ServiceException {
		Date now = new Date();
		String actor = UserUtils.getCurrentUser().getUsername();
		if (mapper.revoke(key.getOid(), key.getTenantId(), key.getLockVersion(), now,
				actor, reason, now) != 1) {
			throw new ServiceException(BaseSystemMessage.updateFail());
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public void markUsed(String keyId, Date usedAt, String sourceIp) throws ServiceException {
		if (mapper.markUsed(keyId, usedAt, sourceIp) != 1) {
			throw new ServiceException(BaseSystemMessage.updateFail());
		}
	}
}
