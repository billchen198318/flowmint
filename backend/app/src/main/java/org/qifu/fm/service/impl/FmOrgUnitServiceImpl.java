package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgUnit;
import org.qifu.fm.mapper.FmOrgUnitMapper;
import org.qifu.fm.service.IFmOrgUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgUnitServiceImpl extends BaseService<FmOrgUnit, String> implements IFmOrgUnitService {
	private final FmOrgUnitMapper orgUnitMapper;

	public FmOrgUnitServiceImpl(FmOrgUnitMapper orgUnitMapper) {
		this.orgUnitMapper = orgUnitMapper;
	}

	@Override
	protected IBaseMapper<FmOrgUnit, String> getBaseMapper() {
		return orgUnitMapper;
	}
}
