package org.qifu.fm.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.qifu.base.exception.ServiceException;
import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.core.model.User;
import org.qifu.core.util.UserUtils;
import org.qifu.fm.entity.FmDataAction;
import org.qifu.fm.mapper.FmDataActionMapper;
import org.qifu.fm.service.IFmDataActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(
		propagation = Propagation.REQUIRED,
		timeout = 300,
		readOnly = true)
public class FmDataActionServiceImpl extends BaseService<FmDataAction, String>
		implements IFmDataActionService {

	private final FmDataActionMapper mapper;

	public FmDataActionServiceImpl(FmDataActionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmDataAction, String> getBaseMapper() {
		return mapper;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public boolean updateOptimistic(FmDataAction action,
			Integer expectedLockVersion) throws ServiceException {
		User currentUser = UserUtils.getCurrentUser();
		action.setUuserid(currentUser == null
				? "SYSTEM" : currentUser.getUsername());
		action.setUdate(new Date());
		Map<String, Object> params = new HashMap<>();
		params.put("action", action);
		params.put("expectedLockVersion", expectedLockVersion);
		return mapper.updateOptimistic(params) == 1;
	}
}
