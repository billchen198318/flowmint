package org.qifu.fm.service.impl;

import org.qifu.base.mapper.IBaseMapper;
import org.qifu.base.service.BaseService;
import org.qifu.fm.entity.FmOrgDuty;
import org.qifu.fm.mapper.FmOrgDutyMapper;
import org.qifu.fm.service.IFmOrgDutyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.REQUIRED, timeout = 300, readOnly = true)
public class FmOrgDutyServiceImpl extends BaseService<FmOrgDuty, String>
		implements IFmOrgDutyService {

	private final FmOrgDutyMapper mapper;

	public FmOrgDutyServiceImpl(FmOrgDutyMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected IBaseMapper<FmOrgDuty, String> getBaseMapper() {
		return mapper;
	}
}
