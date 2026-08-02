package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgUnitHead;
import org.qifu.fm.mapper.FmOrgUnitHeadMapper;
import org.qifu.fm.service.IFmOrgUnitHeadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgUnitHeadServiceImpl extends BaseService<FmOrgUnitHead, String>
		implements IFmOrgUnitHeadService {

	private final FmOrgUnitHeadMapper mapper;

	public FmOrgUnitHeadServiceImpl(FmOrgUnitHeadMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmOrgUnitHead, String> getBaseMapper() {
		return mapper;
	}
}
